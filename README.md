# 🍬 Sugar Advisor App

## 📌 Overview

Sugar Advisor is a mobile-first application that helps users **make smarter food and drink decisions** by analyzing sugar content in real time.

The app focuses on **sugar awareness**, enabling individuals and families to monitor, understand, and control daily sugar intake.

---

## 🎯 Problem Statement

Most people:

* Don’t know how much sugar they consume daily
* Can’t interpret nutrition labels quickly
* Make impulsive decisions when buying food

---

## 💡 Solution

Sugar Advisor provides:

* 📷 Camera-based scanning (label & food)
* 📊 Daily sugar tracking
* 👨‍👩‍👧 Family profiles
* ⚠️ Smart recommendations before consumption

---

## 🚀 Key Features

* Barcode & OCR scanning
* Sugar analysis (natural vs added)
* Daily sugar budget tracking
* Family health dashboard
* Smart decision suggestions

---

## 🧱 Tech Stack

### Backend

* Java, Spring Boot / WebFlux
* PostgreSQL, Redis

### Frontend

* iOS (SwiftUI)

### AI Layer

* OCR (Vision API)
* Ingredient parsing (LLM-assisted)

---

## 🏗️ Architecture

Monorepo structure:

* `backend/` → APIs & business logic
* `frontend/` → mobile app
* `ai/` → OCR & sugar analysis
* `shared/` → contracts & constants

---

## ⚡ Getting Started

### Prerequisites

* Docker
* Java 8+
* Node (optional)
* Xcode (for iOS)

### Run locally

```bash
docker-compose up
```

---

## 📈 Roadmap

* MVP: barcode scan + sugar tracking
* V1: OCR + family profiles
* V2: AI insights + recommendations

---

## 🤝 Contribution

This is a personal project for learning & showcasing system design and AI integration.

---

## 📬 Contact

Thanh Nguyen – Backend Engineer | AI-driven product builder
