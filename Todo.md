# TODO List

## 🧱 Setup

* [x] Initialize GitHub repo
* [x] Setup monorepo structure
* [x] Configure Docker (Redis via docker-compose.yml)

---

## ⚙️ Backend

* [x] Create Spring Boot WebFlux project (Java 17, Gradle 8.8)
* [x] Liquibase migrations (schema, users, family, products, consumptions, analysis, indexes, seed data)
* [x] User API (create, get)
* [x] Product API (lookup by barcode, Redis cache)
* [x] Consumption tracking (record, history, daily total)
* [x] Sugar analysis (LOW/MEDIUM/HIGH, budget tracking, recommendation)
* [x] OCR scan endpoint (stubbed — provider pending)
* [x] Swagger UI (http://localhost:8080/swagger-ui.html)
* [x] Integrate OpenFoodFacts (fallback when barcode not in local DB — free, no API key)

---

## 📱 Frontend (iOS)

* [ ] Setup SwiftUI project
* [ ] Login/Profile screen
* [ ] Scan screen
* [ ] Dashboard UI

---

## 🤖 AI Layer

* [ ] Integrate OCR API (provider pending — see Pending.md)
* [ ] Build ingredient parser
* [ ] Create sugar dictionary
* [ ] Sugar estimation logic

---

## 🔗 Integration

* [ ] Connect frontend to backend
* [ ] Test scan → result flow
* [ ] Validate sugar tracking

---

## 🧪 Testing

* [ ] Unit tests (backend)
* [ ] API tests
* [ ] Manual UI testing

---

## 🚀 Deployment (later)

* [ ] Setup cloud environment
* [ ] CI/CD pipeline
* [ ] Monitoring

---

## 🎯 MVP Goal

👉 "User can scan a product and know if they should consume it"
