# Pending Decisions

Decisions that are deferred and need to be resolved before implementing the related feature.

---

## 1. OCR / Vision API Provider

**Context**: The `/scan/ocr` endpoint extracts sugar/ingredient data from food label images.

**Options**:
- Google Cloud Vision API
- Apple Vision Framework (on-device, iOS only)
- AWS Textract
- Open-source (Tesseract)

**Impact**: Affects `ScanController` and `ai/` module implementation. Currently stubbed to return a placeholder response.

---

## 2. Google Sign-In / OAuth Authentication

**Context**: Users will eventually log in with their Google account instead of using a plain `userId`.

**Impact**:
- Requires Spring Security + OAuth2 Resource Server
- `userId` in API paths/bodies will be replaced by JWT claims
- Affects all controllers and service-layer user resolution

**Current approach**: `userId` passed directly as path/body parameter (no auth for MVP).

---

## 3. OpenFoodFacts Integration

**Context**: When a barcode is not found in the local `products` table, the plan is to fall back to the OpenFoodFacts public API.

**Impact**: Affects `ProductService` — needs HTTP client (WebClient) to fetch and cache external product data.

**Current approach**: Returns 404 if barcode not in local DB.
