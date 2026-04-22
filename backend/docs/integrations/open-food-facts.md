# OpenFoodFacts Integration

## Overview

[OpenFoodFacts](https://world.openfoodfacts.org) is a free, open-source food product database (like Wikipedia for food). No API key or account required.

- **License**: Open Database License (ODbL)
- **Coverage**: 3+ million products worldwide
- **Cost**: Free, no rate limits for reasonable usage

## How It Works

When a barcode is requested, the lookup follows this chain:

```
Redis cache → Local DB → OpenFoodFacts API → save to DB + cache → 404 if not found
```

Products fetched from OpenFoodFacts are:
1. Saved to the local `products` table with `source = 'OPEN_FOOD_FACTS'`
2. Cached in Redis for 24 hours

Subsequent requests for the same barcode never hit OpenFoodFacts again.

## API Reference

- **Base URL**: `https://world.openfoodfacts.org/api/v2/product`
- **Endpoint**: `GET /{barcode}`
- **Auth**: None
- **User-Agent**: Must be set (we send `SugarAdvisorApp/1.0`)

### Example

```bash
curl -H "User-Agent: SugarAdvisorApp/1.0" \
  https://world.openfoodfacts.org/api/v2/product/3017620422003
```

### Response fields used

| Field | Maps to |
|-------|---------|
| `status` | `1` = found, `0` or HTTP 404 = not found |
| `product.product_name` | `Product.name` |
| `product.brands` | `Product.brand` |
| `product.nutriments.sugars_100g` | `Product.sugarPer100g` |
| `product.ingredients_text` | `Product.ingredients` |

## Implementation

| File | Role |
|------|------|
| `client/OpenFoodFactsClient.java` | WebClient, API call, response mapping |
| `service/ProductService.java` | Orchestrates the fallback chain |

## Error Handling

| Scenario | Behaviour |
|----------|-----------|
| Product not in OpenFoodFacts (HTTP 404) | Returns `Mono.empty()` → API responds 404 |
| Product not found (status: 0) | Returns `Mono.empty()` → API responds 404 |
| Network / timeout error | Logged as WARN, returns `Mono.empty()` → API responds 404 |

Errors never crash the request — they fall through to a clean 404.
