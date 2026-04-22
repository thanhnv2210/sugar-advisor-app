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

## 3. ~~OpenFoodFacts Integration~~ ✅ Implemented

Resolved — OpenFoodFacts is free/open-source, no API key required. Implemented in `ProductService` with `OpenFoodFactsClient`. Products fetched from OpenFoodFacts are saved to local DB and cached in Redis.
