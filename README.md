# ⚛️ ReactorX — Backend

> Production-grade e-commerce REST API built with Java 17, Spring Boot 3.2, Spring Security, and PostgreSQL. Deployed on Render with Docker.

[![Java](https://img.shields.io/badge/Java-17-orange?logo=java)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Neon-blue?logo=postgresql)](https://neon.tech)
[![Docker](https://img.shields.io/badge/Docker-Containerized-2496ED?logo=docker)](https://www.docker.com)

---

## 🏗️ Architecture

```
ReactorX Backend
├── Auth Layer          JWT + Spring Security (stateless, BCrypt)
├── Product Catalog     Category-filtered, paginated product API
├── Cart System         Session-aware cart with quantity management
├── Order Engine        Checkout flow with Razorpay integration
├── Address Book        Multi-address per user
└── Static Pages        CMS-style static content API
```

## ✨ Features

- **JWT Authentication** — Stateless auth with `jjwt`, BCrypt password hashing, role-based access control via `@PreAuthorize`
- **Product & Category API** — Full CRUD for products with category filtering and media support
- **Cart Management** — Add, update, remove items; cart persists to PostgreSQL via JPA
- **Checkout & Orders** — Order creation with item snapshot, order history per user
- **Multi-address Support** — Users can store and select delivery addresses
- **Docker Ready** — Single `docker-compose.yml` spins up app + DB
- **Deployed on Render** — With Neon PostgreSQL (serverless, SSL-enforced)
- **Health Endpoint** — `/api/health` for uptime monitoring via Spring Actuator

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2 |
| Security | Spring Security + JWT (jjwt) |
| Database | PostgreSQL (Neon serverless) |
| ORM | Spring Data JPA + Hibernate |
| Containerization | Docker + Docker Compose |
| Build | Maven |
| Deployment | Render |

## 📡 API Endpoints

```
POST   /api/auth/register       Register new user
POST   /api/auth/login          Login, returns JWT

GET    /api/products            List all products
GET    /api/products/{id}       Product detail
GET    /api/categories          All categories

GET    /api/cart                View cart
POST   /api/cart                Add item to cart
PUT    /api/cart/{id}           Update item quantity
DELETE /api/cart/{id}           Remove item

POST   /api/checkout            Create order
GET    /api/orders              Order history

GET/POST /api/address           Address management
```

## 🚀 Run Locally

```bash
git clone https://github.com/binoremohapatra/ReactorX-Backend.git
cd ReactorX-Backend

# With Docker (recommended)
docker-compose up --build

# Without Docker
# Set env vars: DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD, JWT_SECRET
mvn spring-boot:run
```

### Environment Variables

```env
DATABASE_URL=jdbc:postgresql://<host>/<db>
DATABASE_USERNAME=your_user
DATABASE_PASSWORD=your_password
JWT_SECRET=your_base64_secret
```

## 📁 Project Structure

```
src/main/java/com/reactorx/
├── config/         SecurityConfig, WebConfig, CORS, DB init
├── controller/     Auth, Product, Cart, Order, Checkout, Address
├── service/        Business logic layer
├── repository/     Spring Data JPA repositories
├── entity/         JPA entities (User, Product, Order, CartItem…)
├── dto/            Request/Response DTOs
├── security/       JwtAuthenticationFilter, JwtTokenProvider
└── exception/      GlobalExceptionHandler
```

## 🔗 Frontend

[ReactorX Frontend →](https://github.com/binoremohapatra/ReactorX-Frontend)

---

**Built by [Binore Mohapatra](https://github.com/binoremohapatra)**
