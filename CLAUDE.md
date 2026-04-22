# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Sugar Advisor App — a mobile-first application for sugar awareness. Users can scan product barcodes/labels to get real-time sugar analysis and track daily consumption against personalized budgets. Supports family profiles.

**Key product principle**: Focus on decision-making ("Know before you consume"), NOT generic calorie tracking.

## Tech Stack

- **Backend**: Java, Spring Boot/WebFlux, PostgreSQL, Redis, Liquibase
- **Frontend**: iOS (SwiftUI)
- **AI**: Vision API (OCR), LLM-assisted ingredient parsing
- **External**: OpenFoodFacts API (product/barcode data)
- **API contracts**: OpenAPI 3.0.3 (`shared/api-contracts/openapi.yaml`)

## Architecture

Monorepo with loose coupling and clear boundaries designed for easy future splitting:

```
backend/    → Spring Boot APIs (controller/ → service/ → domain/ → repository/ → dto/)
frontend/   → iOS SwiftUI app (Features/, Core/, Networking/, Models/)
ai/         → OCR & parsing (prompts/, services/, experiments/)
shared/     → OpenAPI specs, JSON schemas, sugar dictionary
```

Backend API base: `http://localhost:8080/api`

## Database

PostgreSQL with Liquibase migrations in `backend/src/main/resources/db/changelog/`:

- `001` → `sugar_advisor` schema
- `002` → `users` (id, name, age, weight, daily_sugar_limit)
- `003` → `family_members` (linked to users)
- `004` → `products` (barcode unique constraint, sugar_per_100g, ingredients)
- `005` → `consumptions` (FK to users, family_members, products)
- `006` → `sugar_analysis` (sugar_level: LOW/MEDIUM/HIGH, is_exceed_limit, remaining_sugar)
- `007` → Performance indexes on consumptions and sugar_analysis by user + timestamp

## Key API Endpoints (from OpenAPI spec)

| Method | Path | Purpose |
|--------|------|---------|
| POST | `/users` | Create user profile |
| GET | `/users/{userId}` | Get user profile |
| GET | `/products/barcode/{barcode}` | Look up product |
| POST | `/scan/ocr` | Extract info from label image |
| POST | `/analysis/sugar` | Analyze sugar + give recommendation |
| POST | `/consumptions` | Track consumption |
| GET | `/consumptions/{userId}` | Get consumption history |

## Running Locally

**Start infrastructure** (PostgreSQL + Redis):
```bash
docker-compose up
```

**Backend** (from `backend/`):
```bash
# First time — generate the Gradle wrapper
gradle wrapper

# Build
./gradlew build

# Run
./gradlew bootRun

# Test
./gradlew test

# Single test class
./gradlew test --tests "com.sugaradvisor.SugarAdvisorApplicationTests"
```

API available at `http://localhost:8080/api`

Prerequisites: Docker, Java 17, Gradle, Xcode (iOS frontend)

## Development Phases

- **Phase 1 (MVP)**: User profile, barcode scanning, sugar tracking, basic dashboard
- **Phase 2 (Smart)**: OCR scanning, ingredient parsing, family profiles, sugar budget
- **Phase 3 (Intelligence)**: AI recommendations, fruit recognition, behavior insights

Development order: Backend-first → API-driven → Incremental UI → AI integration last.
