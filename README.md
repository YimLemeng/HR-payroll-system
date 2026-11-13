# HR & Payroll Management System REST API

Enterprise-grade, secure RESTful API for **Human Resources and Payroll Management** built with **Java 25**, **Spring Boot 3.5.x**, **PostgreSQL**, **Redis**, and **Spring Security 6**.

Designed with clean architecture, DTO isolation, comprehensive validation, robust security, distributed token blacklisting with Redis, and automated database migrations.

---

## 1. Technology Stack

- **Language & Runtime**: Java 25 (LTS)
- **Framework**: Spring Boot 3.5.x / Spring Web MVC
- **Database & Persistence**: PostgreSQL, Spring Data JPA / Hibernate, Flyway Migrations
- **Cache & Token Denylist**: Redis (Lettuce client) with in-memory fallback
- **Security**: Spring Security 6, Stateless JWT (JJWT 0.12.x), BCrypt password hashing, method-level `@PreAuthorize`
- **Validation**: Jakarta Bean Validation (`@Valid`, `@NotBlank`, `@PositiveOrZero`, `@Email`, etc.)
- **Documentation**: Springdoc OpenAPI with interactive Swagger UI and Bearer JWT authentication
- **DevOps**: Multi-stage `Dockerfile`, `docker-compose.yml`, and `.env.example`

---

## 2. Role-Based Access Control (RBAC)

The system enforces **two administrative roles**:
1. **`ADMIN`**: Complete administrative and user management authority.
2. **`HR`**: Human resources operational authority (employees, departments, attendance, leaves, payroll, reports).

> [!IMPORTANT]
> **No `EMPLOYEE` login role exists.** Employees are master records managed by `ADMIN` and `HR`. Employees do not authenticate directly to this system.

---

## 3. Security & Token Strategy

- **Stateless Authentication**: Login via `POST /api/auth/login` returns a signed Bearer JWT.
- **Token Blacklisting**: Logout immediately blacklists the JWT in Redis (with automatic TTL matching token expiration) and falls back to concurrent memory cache if Redis is temporarily unreachable.
- **Bootstrap Administrator**: Automatically creates an initial active `ADMIN` on startup if none exists, using environment variables (`BOOTSTRAP_ADMIN_USERNAME`, `BOOTSTRAP_ADMIN_EMAIL`, `BOOTSTRAP_ADMIN_PASSWORD`).

---

## 4. Quick Start & Local Development

### Prerequisites
- Java 25 JDK
- PostgreSQL 16+
- Redis 7+ (optional; application includes in-memory token fallback if Redis is unavailable)

### Running Locally with Maven
1. Clone the repository:
   ```bash
   git clone https://github.com/YimLemeng/HR-payroll-system.git
   cd HR-payroll-system
   ```

2. Configure environment:
   ```bash
   cp .env.example .env
   ```

3. Build and run:
   ```bash
   ./mvnw clean spring-boot:run
   ```

4. Access Swagger UI:
   - OpenAPI Documentation: `http://localhost:8080/swagger-ui.html`

---

## 5. Deployment with Docker Compose

Deploy the complete stack (PostgreSQL + Redis + Spring Boot API) with Docker Compose:

```bash
docker compose up -d --build
```

Check service status:
```bash
docker compose ps
```

View application logs:
```bash
docker compose logs -f api
```
