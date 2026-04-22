# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Sugar Advisor App — a mobile-first application for sugar awareness. Users can scan product barcodes/labels to get real-time sugar analysis and track daily consumption against personalized budgets. Supports family profiles.

**Key product principle**: Focus on decision-making ("Know before you consume"), NOT generic calorie tracking.

## Tech Stack

- **Backend**: Java 17, Spring Boot WebFlux 3.2.5, Gradle 8.8, PostgreSQL (R2DBC), Redis, Liquibase
- **Frontend**: iOS (SwiftUI) — in progress (see `frontend/` structure below)
- **AI**: OCR + ingredient parsing — provider pending (see `Pending.md`)
- **External**: OpenFoodFacts API (free, no key) — `client/OpenFoodFactsClient.java`
- **API docs**: Swagger UI at `http://localhost:8080/swagger-ui.html`
- **API contracts**: `shared/api-contracts/openapi.yaml`

## Architecture

Monorepo with loose coupling designed for easy future splitting:

```
backend/    → Spring Boot WebFlux APIs (controller/ → service/ → domain/ → repository/ → dto/)
frontend/   → iOS SwiftUI app
  SugarAdvisor.xcodeproj       ← open with: open frontend/SugarAdvisor.xcodeproj
  SugarAdvisor/
    App/                        ← entry point (SugarAdvisorApp.swift, ContentView.swift)
    Core/Network/APIClient.swift  ← base URL config (update for physical device)
    Core/Models/Models.swift
    Core/Session/UserSession.swift
    Features/Dashboard/
    Features/Scan/
    Features/History/
    Features/Launch/
ai/         → OCR & parsing (not yet started)
shared/     → OpenAPI specs, JSON schemas
```

Backend base URL: `http://localhost:8080/api`

## Running Locally

**Infrastructure** (Redis only — PostgreSQL reuses existing instance on port 54320):
```bash
docker-compose up -d redis
```

**Backend** (from `backend/`):
```bash
./gradlew bootRun          # start API
./gradlew clean build -x test  # compile only
./gradlew test             # run tests
./gradlew test --tests "com.sugaradvisor.<ClassName>"  # single test
```

**Stop:**
```bash
lsof -ti:8080 | xargs kill -9
docker stop sugar-advisor-redis
```

**iOS app (from `frontend/`):**
```bash
# Build & run on simulator
xcodebuild -project SugarAdvisor.xcodeproj -scheme SugarAdvisor \
  -destination 'platform=iOS Simulator,id=E72627DD-965B-4FFA-B203-5E882C023491' build
xcrun simctl install E72627DD-965B-4FFA-B203-5E882C023491 \
  ~/Library/Developer/Xcode/DerivedData/SugarAdvisor-*/Build/Products/Debug-iphonesimulator/SugarAdvisor.app
xcrun simctl launch E72627DD-965B-4FFA-B203-5E882C023491 thanh.nguyen.SugarAdvisor

# Build & install on physical device (ThanhNguyen iPhone 16 Pro, iOS 26.4)
xcodebuild -project SugarAdvisor.xcodeproj -scheme SugarAdvisor \
  -destination 'platform=iOS,id=00008140-00065D441180801C' -allowProvisioningUpdates build
xcrun devicectl device install app --device 27B7E6F4-2F84-5CFB-A858-A31ECD350909 \
  ~/Library/Developer/Xcode/DerivedData/SugarAdvisor-*/Build/Products/Debug-iphoneos/SugarAdvisor.app
xcrun devicectl device process launch --device 27B7E6F4-2F84-5CFB-A858-A31ECD350909 thanh.nguyen.SugarAdvisor
```

See `backend/README.md` and `frontend/README.md` for full details.

## Database

- **Host**: `localhost:54320` (existing shared PostgreSQL instance)
- **DB / Schema**: `sugar_advisor` / `sugar_advisor`
- **Credentials**: `admin` / `admin`
- Liquibase migrations in `backend/src/main/resources/db/changelog/changes/`
- Migration guide: `backend/src/main/resources/db_migration.md`

### Migration order

| File | Creates |
|------|---------|
| `001` | `sugar_advisor` schema |
| `002` | `users` |
| `003` | `family_members` |
| `004` | `products` (barcode unique) |
| `005` | `consumptions` |
| `006` | `sugar_analysis` |
| `007` | Performance indexes |
| `008` | Seed products (10 common items) |

**First-time setup** — schema must be created manually before first run:
```bash
psql -h localhost -p 54320 -U admin -d sugar_advisor -c "CREATE SCHEMA IF NOT EXISTS sugar_advisor;"
liquibase --defaults-file=src/main/resources/liquibase.properties update
```

## Key API Endpoints

| Method | Path | Purpose |
|--------|------|---------|
| POST | `/api/users` | Create user profile |
| GET | `/api/users/{userId}` | Get user profile |
| GET | `/api/products/barcode/{barcode}` | Look up product (local DB → OpenFoodFacts fallback → Redis-cached 24h) |
| POST | `/api/scan/ocr` | Extract info from label image (stubbed) |
| POST | `/api/analysis/sugar` | Analyze sugar + give recommendation |
| POST | `/api/consumptions` | Record consumption |
| GET | `/api/consumptions/{userId}` | Get consumption history |

## Important Implementation Notes

- **Sugar levels**: LOW < 5g, MEDIUM 5–15g, HIGH ≥ 15g per serving
- **Default daily limit**: 50g (WHO upper bound) if user hasn't set one
- **Product.createdAt is intentionally absent** from the domain entity — `Jackson2JsonRedisSerializer` cannot handle `LocalDateTime`; the DB column still exists but is not mapped. See `backend/docs/troubleshooting/redis-localdatetime-serialization.md`.
- **Gradle version**: must use `./gradlew` (pinned to 8.8). Running `gradle` directly uses system Gradle 9.x which is incompatible with Spring Boot 3.2.x.
- **OCR and Google Auth** are stubbed/absent — decisions pending in `Pending.md`.
- **iOS base URL**: `APIClient.swift` uses `http://192.168.1.3:8080/api` (Mac's local IP for physical device). Change back to `localhost` for simulator-only builds.
- **Xcode version**: must use Xcode 26.4.1+ (iOS 26.4 SDK required for physical device). Set active Xcode with `sudo xcode-select -s /Applications/Xcode.app/Contents/Developer`.
- **Physical device signing**: team ID `R6B94B8A4D`, bundle ID `thanh.nguyen.SugarAdvisor`. First install requires trusting the cert on device: Settings → General → VPN & Device Management.

## Troubleshooting

Common issues and fixes in `backend/docs/troubleshooting/`:

| File | Issue |
|------|-------|
| `gradle-spring-boot-compatibility.md` | Gradle 9 breaks Spring Boot 3.2.x build |
| `liquibase-xml-namespace.md` | Missing `xsi:schemaLocation` in changeset XML |
| `liquibase-schema-not-found.md` | Schema must be pre-created before first Liquibase run |
| `redis-localdatetime-serialization.md` | `LocalDateTime` fields crash Redis serialization |

## Development Phases

- **Phase 1 (MVP) — in progress**:
  - ✅ Backend API complete
  - ✅ iOS SwiftUI app scaffolded (Dashboard, Scan, History, Launch screens)
  - ✅ App runs on iPhone 17 Pro simulator and ThanhNguyen's physical iPhone 16 Pro (iOS 26.4)
  - 🔄 UI development ongoing — this session focuses on frontend
- **Phase 2 (Smart)**: OCR scanning, ingredient parsing, family profiles, sugar budget
- **Phase 3 (Intelligence)**: AI recommendations, fruit recognition, behavior insights

Development order: Backend-first → API-driven → Incremental UI → AI integration last.

## Multi-Session Collaboration

Two sessions working in parallel (this MacBook on frontend/UI + another laptop on backend).
**Update this file whenever there is a significant change** (new screens, API changes, env setup changes) so both sessions stay in sync.
