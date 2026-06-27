# 🚀 Spring Boot Starter Kit

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Docker](https://img.shields.io/badge/Docker-ready-blue?logo=docker)](https://www.docker.com/)
[![CI/CD](https://img.shields.io/github/actions/workflow/status/yourusername/springboot-starter-kit/ci.yml?label=CI%2FCD)](https://github.com/yourusername/springboot-starter-kit/actions)

> **Stop copy-pasting boilerplate. Clone this, rename the package, and ship.**

A production-ready Spring Boot 3 REST API template with everything you need baked in — JWT auth, rate limiting, Swagger docs, Docker, GitHub Actions CI/CD, and more.

---

## ✨ Features

| Feature | Details |
|---|---|
| 🔐 **JWT Authentication** | Access token (15 min) + Refresh token (7 days) with rotation |
| 👮 **Role-Based Access Control** | `ROLE_USER` and `ROLE_ADMIN` via `@PreAuthorize` |
| 🚦 **Rate Limiting** | Per-IP rate limiting using Bucket4j (configurable) |
| 📄 **OpenAPI 3 / Swagger UI** | Auto-generated docs at `/swagger-ui.html` |
| 🛡️ **Global Exception Handling** | Consistent `ApiResponse<T>` across all endpoints |
| 🗃️ **Flyway Migrations** | Version-controlled schema with rollback support |
| 🐳 **Docker + Docker Compose** | Multi-stage build, nginx reverse proxy, MySQL |
| ⚙️ **CI/CD via GitHub Actions** | Test → Build → Push Docker → Deploy to EC2 |
| 📊 **Actuator** | `/actuator/health`, `/actuator/metrics` |
| 🔍 **Request ID Tracking** | MDC-based request ID in every log line |
| 🌍 **CORS Configured** | Configurable via environment variables |
| ✅ **Bean Validation** | Jakarta Validation on all request DTOs |

---

## 📁 Project Structure

```
src/main/java/com/starterkit/
├── config/              # Security, Swagger, DataInitializer
├── controller/          # AuthController, UserController, HealthController
├── dto/
│   ├── request/         # RegisterRequest, LoginRequest, RefreshTokenRequest
│   └── response/        # ApiResponse<T>, AuthResponse, UserResponse
├── entity/              # User, Role, RefreshToken
├── exception/           # GlobalExceptionHandler + custom exceptions
├── filter/              # JwtAuthFilter, RateLimitFilter, RequestIdFilter
├── repository/          # JPA repositories
├── security/            # JwtService, UserDetailsServiceImpl
└── service/             # AuthService, UserService
```

---

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- Docker & Docker Compose

### Option 1 — Run locally (H2 in-memory, zero setup)

```bash
git clone https://github.com/raahulllkushwaha/springboot-starter-kit.git
cd springboot-starter-kit

mvn spring-boot:run
```

App starts at `http://localhost:8080`  
Swagger UI: `http://localhost:8080/swagger-ui.html`  
H2 Console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:starterkit`)

### Option 2 — Docker Compose (MySQL + Nginx)

```bash
cp .env.example .env
# Edit .env with your values

docker compose up -d
```

---

## 🔑 API Endpoints

### Auth (Public)
```
POST /api/v1/auth/register    → Register new user
POST /api/v1/auth/login       → Login, get tokens
POST /api/v1/auth/refresh     → Refresh access token
POST /api/v1/auth/logout      → Revoke refresh token
```

### Users (Protected)
```
GET  /api/v1/users/me         → Get current user (any authenticated)
GET  /api/v1/users/{id}       → Get user by ID (admin or self)
GET  /api/v1/users            → List all users (admin only)
DELETE /api/v1/users/{id}     → Delete user (admin only)
```

### Other
```
GET /api/v1/ping              → Health check
GET /actuator/health          → Spring Actuator health
```

---

## 🔐 Authentication Flow

```
1. Register  →  POST /api/v1/auth/register
               Body: { name, email, password }

2. Login     →  POST /api/v1/auth/login
               Body: { email, password }
               Returns: { accessToken, refreshToken, ... }

3. Use API   →  Add header: Authorization: Bearer <accessToken>

4. Refresh   →  POST /api/v1/auth/refresh
               Body: { refreshToken }
               Returns: new accessToken + rotated refreshToken

5. Logout    →  POST /api/v1/auth/logout
               Body: { refreshToken }
```

**Default admin credentials** (seeded on startup):
- Email: `admin@example.com`
- Password: `Admin@1234`

> ⚠️ Change these via environment variables `ADMIN_EMAIL` and `ADMIN_PASSWORD` before deploying.

---

## ⚙️ Configuration

All key settings are environment-variable driven:

| Variable | Default | Description |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `dev` | `dev` (H2) or `prod` (MySQL) |
| `DB_HOST` | `localhost` | MySQL host |
| `DB_NAME` | `starterkit` | Database name |
| `DB_USERNAME` | — | MySQL username |
| `DB_PASSWORD` | — | MySQL password |
| `JWT_SECRET` | (dev default) | **Must change in prod** |
| `JWT_ACCESS_EXPIRY` | `900000` | Access token TTL (ms) |
| `JWT_REFRESH_EXPIRY` | `604800000` | Refresh token TTL (ms) |
| `CORS_ALLOWED_ORIGINS` | `localhost:3000` | Comma-separated origins |

---

## 🐳 Docker

### Build image manually
```bash
docker build -t springboot-starter-kit .
```

### Run with Docker Compose
```bash
# Production (MySQL + Nginx + App)
docker compose up -d

# Development (only MySQL, run app locally)
docker compose -f docker-compose.dev.yml up -d
```

---

## 🔄 CI/CD (GitHub Actions)

The pipeline runs on every push to `main`:

```
Push to main
    ↓
[Test] → mvn test (with MySQL service container)
    ↓
[Docker] → Build & push image to Docker Hub
    ↓
[Deploy] → SSH into EC2, docker compose pull && up
```

### Required GitHub Secrets

| Secret | Description |
|---|---|
| `DOCKERHUB_USERNAME` | Docker Hub username |
| `DOCKERHUB_TOKEN` | Docker Hub access token |
| `EC2_HOST` | EC2 public IP / domain |
| `EC2_USER` | EC2 SSH username (e.g. `ubuntu`) |
| `EC2_SSH_KEY` | EC2 private key (PEM contents) |

---

## 🛡️ Rate Limiting

Configured in `application.yml`:

```yaml
app:
  rate-limit:
    enabled: true
    capacity: 20          # max tokens in bucket
    refill-tokens: 20     # tokens added per window
    refill-seconds: 60    # window size
```

Returns `429 Too Many Requests` when limit exceeded.

---

## 🏗️ Customising for Your Project

1. **Rename package**: Find & replace `com.starterkit` → `com.yourcompany.yourapp`
2. **Update `application.yml`**: Change `spring.application.name`
3. **Update `SwaggerConfig.java`**: Set your name/contact/GitHub URL
4. **Update `docker-compose.yml`**: Change container names and image names
5. **Add your entities**: Create entities → migration SQL → repository → service → controller
6. **Deploy**: Push to `main`, GitHub Actions handles the rest

---

## 🧪 Running Tests

```bash
# Unit + integration tests (uses H2)
mvn test

# Skip tests (for fast build)
mvn package -DskipTests
```

---

## 🔀 Two Auth Approaches

This starter supports two authentication strategies.

### Approach 1: JWT (default — `main` branch)

```
POST /login → accessToken + refreshToken returned
Client: Authorization: Bearer <token> OR HTTP-only cookie
Server: validates JWT signature — zero DB calls
```
✅ Stateless, mobile-friendly, microservices-ready
⚠️ Token revocation requires refresh token invalidation

### Approach 2: Spring Session + JDBC (`feature/spring-session` branch)

```
POST /login → SESSION cookie set automatically
Client: cookie on every request (browser handles it)
Server: reads SecurityContext from JDBC session store
```
✅ No custom JWT filter — uses Spring Security built-in flow
✅ Instant revocation (delete session from DB)
✅ Survives server restarts (JDBC-backed)
⚠️ Not ideal for mobile/API clients

### Switch to Spring Session
```bash
git checkout feature/spring-session
# Set in .env: SPRING_PROFILES_ACTIVE=session
mvn spring-boot:run
```
---

## 📦 Tech Stack

- **Java 21** + **Spring Boot 3.3.0**
- **Spring Security 6** (JWT, RBAC)
- **jjwt 0.12** (JWT library)
- **Bucket4j 8** (Rate limiting)
- **SpringDoc OpenAPI 3 / Swagger UI**
- **Flyway** (Database migrations)
- **MySQL 8** / **H2** (dev)
- **Lombok**
- **Docker** + **Nginx**
- **GitHub Actions**

---

## 🤝 Contributing

PRs are welcome! If you find this useful, please ⭐ the repo.

1. Fork the repo
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📜 License

MIT © [Rahul Kushwaha](https://github.com/yourusername)

---

<p align="center">If this saved you hours of setup, consider giving it a ⭐</p>
