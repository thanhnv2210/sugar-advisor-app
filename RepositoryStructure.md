# 📁 Repository Structure

## 🧭 Overview

Monorepo approach for fast development and full-system visibility.

---

## 📦 Structure

sugar-advisor-app/
├── backend/          # Spring Boot APIs
├── frontend/         # iOS app
├── ai/               # OCR + parsing logic
├── shared/           # contracts & constants
├── docs/             # documentation
├── scripts/          # dev scripts
├── infra/            # deployment configs
└── docker-compose.yml

---

## 🧱 Backend

* controller/
* service/
* domain/
* repository/
* dto/

---

## 📱 Frontend (iOS)

* Features/
* Core/
* Networking/
* Models/

---

## 🤖 AI Layer

* prompts/
* services/
* experiments/

---

## 🔗 Shared

* OpenAPI specs
* JSON schemas
* Sugar dictionary

---

## 🧠 Principles

* Loose coupling
* Clear boundaries
* Easy to split later
