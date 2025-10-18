# 🛁 BathForge
![Java](https://img.shields.io/badge/Java-17-blue?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-green?logo=springboot)
![React](https://img.shields.io/badge/React-18-61DAFB?logo=react)
![TypeScript](https://img.shields.io/badge/TypeScript-5.x-3178C6?logo=typescript)
![License](https://img.shields.io/badge/License-MIT-yellow)

> **BathForge** is a full‑stack web application for bathroom design & product management. It auto‑imports 3D assets and images into a structured database, exposes a clean REST API, and ships with a React UI.

---

## 🌐 Overview
- **Backend:** Java 17 · Spring Boot · Gradle · JPA
- **Frontend:** React 18 · TypeScript · Axios
- **Database:** H2 (dev) / PostgreSQL (prod)

### Highlights
- 🏗️ **Auto‑import** of assets from `frontend/public/assets`
- 🎨 **Predefined categories & colors** with validations
- 🔍 **Filtering & search** by category, price, mounting type, name
- 🔑 **JWT-ready** client with typed controllers & unified error handling
- 💾 **Self‑initializing** database + admin tools

---

## 📁 Project Structure
```
bathforge/
├── backend/                 # Spring Boot REST API
│   ├── model/               # Entities: Category, Product, Color, ProductColor
│   ├── service/             # Business logic & AssetScannerService
│   ├── controller/          # REST endpoints (Categories, Colors, Products, Admin)
│   └── repository/          # Spring Data JPA repositories
└── frontend/                # React + TypeScript app
    ├── src/controllers/     # Typed API controllers (BaseController, Auth, etc.)
    └── public/assets/       # 3D models (.glb/.gltf) & images (.jpg/.png)
```

---

## 🧠 Database Model (ER)
**Entities**
- **Category** – name, description
- **Product** – name, description, `priceRange` (LOW/MEDIUM/HIGH), `modelPath`, `mountingType` (FLOOR/WALL/FREESTANDING), `categoryId`
- **Color** – name, `hexCode`, `categoryId`
- **ProductColor** – junction (many‑to‑many Product ↔ Color)

**Behavior**
- Categories & colors **pre‑populated** per specification
- Products **auto‑created** by scanning `frontend/public/assets`
- Robust **validation** (hex codes, enums), **global exception handling**

---

## ⚙️ Configuration (backend `application.properties`)
```properties
# Auto‑import
bathforge.auto-import.enabled=true
bathforge.auto-import.skip-if-products-exist=true

# DB (dev default is H2 in‑memory)
spring.h2.console.enabled=true
```
**Auto‑import behavior**
- `enabled=true` + `skip-if-products-exist=true` → import only if empty (default)
- `enabled=true` + `skip-if-products-exist=false` → always import (may duplicate)
- `enabled=false` → no auto‑import (manual only)

---

## 🚀 Quick Start

### 1) Backend
```bash
cd backend
./gradlew bootRun
# Dev DB console: http://localhost:8080/h2-console
# JDBC: jdbc:h2:mem:testdb | user: sa | password: (empty)
```

### 2) Frontend
```bash
cd frontend
npm install
npm start
# App: http://localhost:3000 (proxies API to :8080)
```

---

## 🔌 REST API (selected)
**Categories**
- `GET /api/categories`, `GET /api/categories/{id}`

**Colors**
- `GET /api/colors`
- `GET /api/colors/category/{categoryId}`
- `GET /api/colors/category/name/{categoryName}`

**Products**
- `GET /api/products`, `GET /api/products/{id}`
- `GET /api/products/category/{categoryId}` / `.../category/name/{categoryName}`
- `GET /api/products/price/{priceRange}` / `.../mounting/{mountingType}`
- `GET /api/products/search?name=...`
- `GET /api/products/filter?categoryId=ID&priceRange=LOW&mountingType=WALL`
- `POST /api/products/{productId}/colors/{colorId}` (link color)

**Admin**
- `POST /api/admin/scan-assets`
- `GET /api/admin/health`

---

## 🧩 Frontend Controllers (TypeScript)
- Extend **`BaseController`** for shared HTTP (get/post/put/delete)
- Central **error handling**, **JWT token** attachment via interceptors
- Strong typing for requests/responses for IntelliSense & safety

**Import examples**
```ts
import { systemController } from "./controllers";
const res = await systemController.testConnection();

import { systemController, bathroomController, authController } from "./controllers";
import { ApiResponse, Bathroom, CreateBathroomRequest } from "./controllers";
```

---

## 🧪 Dev Tips
- Backend tests: `./gradlew test`
- Frontend tests: `npm test`
- Quick API check:
```bash
curl http://localhost:8080/api/products
```

---

## 📦 Production
- **Backend**: `./gradlew build` → jar in `backend/build/libs/`
- **Frontend**: `npm run build` → static build in `frontend/build/`
- Switch DB to PostgreSQL in `application.properties` for production.

---

## 📝 License
MIT
