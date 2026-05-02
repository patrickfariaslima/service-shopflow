# ShopFlow — E-Commerce REST API

A full-featured e-commerce backend built with Java 21 and Spring Boot 3, implementing JWT authentication, role-based access control, shopping cart management, order processing with stock control, and an admin dashboard.

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-6DB33F?style=flat&logo=spring-boot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=flat&logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7-DC382D?style=flat&logo=redis&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat&logo=docker&logoColor=white)
![Coverage](https://img.shields.io/badge/Coverage-JaCoCo-brightgreen?style=flat)

---

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Features](#features)
- [Getting Started](#getting-started)
- [API Reference](#api-reference)
- [Database Schema](#database-schema)
- [Testing](#testing)
- [Project Structure](#project-structure)
- [Author](#author)

---

## Overview

ShopFlow is a RESTful API that covers the complete lifecycle of an e-commerce platform: user registration and authentication, product catalog browsing, cart management, order checkout with automatic stock decrement, and full administrative control over products, orders, and users.

The project follows a classic layered architecture (Controller → Service → Repository) using well-established enterprise Java patterns, making it straightforward to navigate and extend.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 (LTS) |
| Framework | Spring Boot 3.5 |
| Security | Spring Security 6 + JWT (jjwt 0.12.6) |
| Persistence | Spring Data JPA / Hibernate 6 |
| Database | PostgreSQL 16 |
| Migrations | Flyway |
| Token revocation | Redis 7 (deny list) |
| Validation | Jakarta Bean Validation |
| Boilerplate reduction | Lombok |
| API Documentation | SpringDoc OpenAPI 3 (Swagger UI) |
| Testing | JUnit 5 + Mockito + JaCoCo |
| Build | Maven 3.9 |
| Infrastructure | Docker Compose |

---

## Architecture

```
HTTP Request
     │
     ▼
[ JwtAuthFilter ]          ← Validates Bearer token on every request
     │
     ▼
[ Controller Layer ]       ← Receives HTTP, delegates to service
     │
     ▼
[ Service Layer ]          ← Business rules, transactions (@Transactional)
     │
     ▼
[ Repository Layer ]       ← Spring Data JPA interfaces
     │
     ▼
[ PostgreSQL 16 ]          ← Relational data (Flyway-managed schema)
```

Each service is defined by an interface and implemented by a concrete `@Service` class, enabling full decoupling for unit testing with Mockito.

---

## Features

### Authentication & Authorization
- User registration with BCrypt password hashing
- JWT-based login returning an access token (15 min) and a refresh token (7 days)
- Token refresh endpoint
- Logout with server-side token invalidation via Redis deny list
- Two roles: `CUSTOMER` and `ADMIN`
- Method-level security with `@PreAuthorize`

### Product Catalog
- Paginated product listing with filters: category, name, price range
- Full CRUD for products (admin only)
- Full CRUD for categories (admin only)
- Soft delete: products are deactivated, not removed

### Shopping Cart
- One persistent cart per authenticated user
- Add, update quantity, and remove items
- Stock availability validation on every add/update
- Cart summary with calculated totals

### Order Management
- Checkout converts the active cart into an order with automatic stock decrement
- Order history for the authenticated user
- Order detail view
- Admin view of all orders
- Order status progression: `PENDING` → `PAID` → `PROCESSING` → `SHIPPED` → `DELIVERED` / `CANCELLED` / `FAILED`
- Automatic stock refund when an order is cancelled

### Stock Control
- Stock overview with low-stock alerts (configurable threshold per product)
- Manual stock adjustment by admins (IN/OUT movements)
- Full movement history per product, including the originating order when applicable

### Admin Dashboard
- Total orders count
- Today's revenue
- List of products below stock threshold

### User Management (Admin)
- List all users
- Activate / deactivate user accounts

---

## Getting Started

### Prerequisites

- [JDK 21](https://adoptium.net)
- [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- [Maven 3.9+](https://maven.apache.org/) (or use the included `./mvnw` wrapper)

### 1. Clone the repository

```bash
git clone https://github.com/patrickfarias/shopflow.git
cd shopflow
```

### 2. Start the infrastructure

```bash
docker-compose up -d
```

This starts:
- **PostgreSQL 16** on port `5432` — database `shopflow`, user `shopflow_user`
- **Redis 7** on port `6379`

### 3. Configure local environment variables

Create the file `src/main/resources/application-local.properties` (already in `.gitignore`):

```properties
DB_USERNAME=shopflow_user
DB_PASSWORD=shopflow_pass
JWT_SECRET_KEY=<your-base64-encoded-256-bit-secret>
```

> The JWT secret must be a Base64-encoded string with at least 256 bits of entropy.  
> Example (Linux/macOS): `openssl rand -base64 32`

### 4. Run the application

```bash
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`.

### 5. Access Swagger UI

```
http://localhost:8080/api-docs
```

Swagger lists all endpoints with request/response schemas and supports authenticated calls via the **Authorize** button (paste a Bearer token).

---

## API Reference

Base URL: `http://localhost:8080/api/v1`

### Authentication

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/auth/register` | Public | Register a new user |
| `POST` | `/auth/login` | Public | Login and receive JWT tokens |
| `POST` | `/auth/refresh` | Public | Refresh the access token |
| `POST` | `/auth/logout` | Token | Invalidate the current token |

**Register / Login payload examples:**

```json
// POST /auth/register
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "secret123"
}

// POST /auth/login
{
  "email": "john@example.com",
  "password": "secret123"
}

// Response
{
  "accessToken": "eyJhbGci...",
  "refreshToken": "eyJhbGci..."
}
```

---

### Products

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/products` | Public | List products (paginated + filters) |
| `GET` | `/products/{id}` | Public | Get product details |
| `POST` | `/products` | Admin | Create product |
| `PUT` | `/products/{id}` | Admin | Update product |
| `DELETE` | `/products/{id}` | Admin | Deactivate product |

**Query parameters for `GET /products`:**

| Parameter | Type | Description |
|---|---|---|
| `categoryId` | Long | Filter by category |
| `name` | String | Filter by name (partial match) |
| `minPrice` | Decimal | Minimum price filter |
| `maxPrice` | Decimal | Maximum price filter |
| `page` | Integer | Page number (default: 0) |
| `size` | Integer | Page size (default: 10) |

---

### Categories

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/products/categories` | Public | List all categories |
| `GET` | `/products/categories/{id}` | Public | Get category details |
| `POST` | `/products/categories` | Admin | Create category |
| `PUT` | `/products/categories/{id}` | Admin | Update category |
| `DELETE` | `/products/categories/{id}` | Admin | Deactivate category |

---

### Cart

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/cart` | Token | View active cart with totals |
| `POST` | `/cart/items` | Token | Add item to cart |
| `PUT` | `/cart/items/{productId}` | Token | Update item quantity |
| `DELETE` | `/cart/items/{productId}` | Token | Remove item from cart |
| `DELETE` | `/cart` | Token | Clear entire cart |

---

### Orders

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/orders` | Token | Checkout (cart → order) |
| `GET` | `/orders` | Token | User's order history |
| `GET` | `/orders/{id}` | Token | Order details |
| `PATCH` | `/orders/{id}/status` | Admin | Update order status |
| `GET` | `/admin/orders` | Admin | All orders (admin view) |

**Order status flow:**

```
PENDING → PAID → PROCESSING → SHIPPED → DELIVERED
                                      ↘ CANCELLED (triggers stock refund)
```

---

### Stock

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/admin/stock` | Admin | Stock overview with low-stock flags |
| `POST` | `/admin/stock/{productId}/adjust` | Admin | Manual stock adjustment |
| `GET` | `/admin/stock/{productId}/history` | Admin | Movement history for a product |

**Adjust stock payload:**

```json
{
  "quantity": 50,
  "type": "IN",
  "reason": "Purchase order #1042"
}
```

---

### Admin Dashboard

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/admin/dashboard` | Admin | Metrics: orders, revenue, low stock |

---

### User Management

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/admin/users` | Admin | List all users |
| `PATCH` | `/admin/users/{id}/deactivate` | Admin | Deactivate a user account |
| `PATCH` | `/admin/users/{id}/activate` | Admin | Reactivate a user account |

---

### Error Responses

All errors follow a consistent envelope:

```json
{
  "status": 404,
  "message": "Product not found",
  "timestamp": "2026-05-02T14:30:00"
}
```

| HTTP Status | Scenario |
|---|---|
| `400` | Validation failure (missing/invalid fields) |
| `401` | Missing or invalid JWT token |
| `403` | Authenticated but insufficient role |
| `404` | Resource not found |
| `409` | Business rule violation (e.g. insufficient stock) |
| `500` | Unexpected server error |

---

## Database Schema

Schema is managed by Flyway. Migrations live in `src/main/resources/db/migration/`.

```
users
├── id, name, email (unique), password (bcrypt), role, active, created_at

categories
├── id, name (unique), description, active

products
├── id, name, description, price, stock_qty, stock_threshold
├── image_url, active, created_at
└── category_id → categories(id)

carts
├── id, created_at
└── user_id → users(id) [unique — one cart per user]

cart_items
├── id, quantity, unit_price
├── cart_id → carts(id)
└── product_id → products(id)

orders
├── id, status, total_amount, address, created_at
└── user_id → users(id)

order_items
├── id, quantity, unit_price
├── order_id → orders(id)
└── product_id → products(id)

stock_movements
├── id, type (IN/OUT), quantity, reason, created_at
├── product_id → products(id)
└── order_id → orders(id) [nullable — set when movement originates from an order]
```

---

## Testing

The project has two test layers:

### Unit tests — `@ExtendWith(MockitoExtension.class)`

All service classes are tested in isolation with Mockito mocks. Covers the full business logic including edge cases (insufficient stock, null thresholds, order cancellation stock refund, token deny list, JWT filter states).

### Controller tests — `@WebMvcTest`

Each controller is tested with `MockMvc` and mocked service dependencies. Verifies HTTP status codes, JSON response structure, authentication/authorization enforcement, and input validation.

### Running the tests

```bash
./mvnw test
```

### Generating the JaCoCo coverage report

```bash
./mvnw test
# Report generated at: target/site/jacoco/index.html
```

---

## Project Structure

```
src/
├── main/
│   ├── java/com/shopflow/shopflow/
│   │   ├── controller/          # REST Controllers
│   │   ├── service/             # Service interfaces + implementations
│   │   │   ├── auth/
│   │   │   ├── admin/
│   │   │   ├── cart/
│   │   │   ├── category/
│   │   │   ├── jwt/
│   │   │   ├── order/
│   │   │   ├── product/
│   │   │   ├── refreshtoken/
│   │   │   ├── stock/
│   │   │   └── user/
│   │   ├── repository/          # Spring Data JPA interfaces
│   │   ├── entity/              # JPA entities
│   │   ├── dto/                 # Request / Response DTOs
│   │   ├── enums/               # OrderStatus, MovementType, UserRole
│   │   ├── security/            # SecurityConfig, JwtAuthFilter
│   │   ├── exception/           # GlobalExceptionHandler, custom exceptions
│   │   └── config/              # SwaggerConfig
│   └── resources/
│       ├── application.properties
│       ├── application-local.properties  ← not committed (git-ignored)
│       └── db/migration/                 ← Flyway SQL migrations
└── test/
    ├── java/com/shopflow/shopflow/
    │   ├── controller/          # @WebMvcTest controller tests
    │   ├── service/             # Mockito unit tests per service
    │   ├── security/            # JwtAuthFilter tests
    │   └── exception/           # GlobalExceptionHandler tests
    └── resources/
        └── application.properties  ← H2 + Flyway disabled for test context
```

---

## Author

**Patrick Farias**  
[GitHub](https://github.com/patrickfarias) · [LinkedIn](https://linkedin.com/in/patrickfarias) · patrickfarias@live.com
