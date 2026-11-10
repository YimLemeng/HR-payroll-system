# HR & Payroll Management System REST API

[![Java](https://img.shields.io/badge/Java-25-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7-DC382D?logo=redis&logoColor=white)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-purple.svg)](LICENSE)

An enterprise-grade, highly secure RESTful backend API for **Human Resources, Attendance, Leave Administration, and Automated Payroll Processing**. Built with clean architecture, strict DTO isolation, comprehensive Jakarta Bean Validation, distributed token revocation, and automated database migrations.

---

## 🏛️ System Architecture

```mermaid
flowchart TD
    subgraph Clients["Client Layer"]
        UI["Web SPA / React Frontend"]
        API_CLIENT["Mobile App / Postman / Swagger UI"]
    end

    subgraph SecurityLayer["Security & Gateway Layer"]
        CORS["CORS & CSRF Configuration"]
        JWT_FILTER["JwtAuthenticationFilter"]
        RBAC["Method Security (@PreAuthorize)"]
    end

    subgraph AppLayer["Application Layer (Spring Boot)"]
        CONTROLLER["REST Controllers (/api/*)"]
        SERVICE["Business Service Layer"]
        MAPPER["MapStruct / Entity Mappers"]
        DTO["Validation & DTOs"]
    end

    subgraph PersistenceLayer["Data & Persistence Layer"]
        JPA["Spring Data JPA / Hibernate"]
        FLYWAY["Flyway Database Migration"]
        REDIS_CLIENT["Lettuce Redis Client"]
    end

    subgraph Infrastructure["Infrastructure & Storage"]
        DB[("PostgreSQL 16 Engine")]
        REDIS_CACHE[("Redis 7 (Distributed Denylist)")]
    end

    UI --> CORS
    API_CLIENT --> CORS
    CORS --> JWT_FILTER
    JWT_FILTER --> RBAC
    RBAC --> CONTROLLER
    CONTROLLER --> DTO
    CONTROLLER --> SERVICE
    SERVICE --> MAPPER
    SERVICE --> JPA
    SERVICE --> REDIS_CLIENT
    JPA --> DB
    FLYWAY --> DB
    REDIS_CLIENT --> REDIS_CACHE
```

---

## 🗄️ Domain Entity Relationship Diagram (ERD)

```mermaid
erDiagram
    DEPARTMENT ||--o{ EMPLOYEE : "employs"
    EMPLOYEE ||--o{ ATTENDANCE : "logs"
    EMPLOYEE ||--o{ LEAVE_REQUEST : "applies"
    EMPLOYEE ||--o{ PAYROLL : "receives"
    USER ||--o{ LEAVE_REQUEST : "reviews"
    USER ||--o{ PAYROLL : "approves"

    DEPARTMENT {
        bigint id PK
        string name UK
        string description
        timestamp created_at
    }

    EMPLOYEE {
        bigint id PK
        string employee_code UK
        string first_name
        string last_name
        string gender
        date date_of_birth
        string email UK
        string phone
        string address
        date hire_date
        decimal basic_salary
        string employment_status
        bigint department_id FK
    }

    ATTENDANCE {
        bigint id PK
        bigint employee_id FK
        date attendance_date
        time check_in_time
        time check_out_time
        string status
        decimal overtime_hours
        string notes
    }

    LEAVE_REQUEST {
        bigint id PK
        bigint employee_id FK
        string leave_type
        date start_date
        date end_date
        integer total_days
        string reason
        string status
        bigint reviewed_by FK
        string review_comments
    }

    PAYROLL {
        bigint id PK
        bigint employee_id FK
        integer period_month
        integer period_year
        decimal basic_salary
        decimal allowance
        decimal overtime_pay
        decimal gross_salary
        decimal deductions
        decimal tax
        decimal net_salary
        string status
        timestamp calculated_at
        bigint approved_by FK
        timestamp paid_at
    }

    USER {
        bigint id PK
        string username UK
        string email UK
        string password
        string role
        boolean active
    }
```

---

## 🔐 Authentication & Token Lifecycle Workflow

```mermaid
sequenceDiagram
    autonumber
    actor User as User (ADMIN / HR)
    participant Filter as JwtAuthenticationFilter
    participant AuthCtrl as AuthController
    participant TokenBlacklist as TokenBlacklistService
    participant Redis as Redis Cache
    participant DB as PostgreSQL DB

    %% Login Flow
    Note over User, DB: Authentication Flow (Login)
    User->>AuthCtrl: POST /api/auth/login (username, password)
    AuthCtrl->>DB: Verify credentials & Active status
    DB-->>AuthCtrl: User authenticated
    AuthCtrl->>AuthCtrl: Generate HMAC-SHA256 JWT Token
    AuthCtrl-->>User: Return 200 OK + Bearer Token + User Details

    %% Authenticated Call
    Note over User, DB: Authorized API Call
    User->>Filter: GET /api/employees (Header: Bearer <Token>)
    Filter->>TokenBlacklist: isTokenBlacklisted(token)
    TokenBlacklist->>Redis: Check key existence (revoked:<token>)
    Redis-->>TokenBlacklist: Key not found (Token is valid)
    Filter->>Filter: Validate signature & expiration
    Filter->>DB: Load UserPrincipal & Set SecurityContext
    Filter-->>User: Process request & return 200 OK

    %% Logout Flow
    Note over User, DB: Logout & Instant Revocation
    User->>AuthCtrl: POST /api/auth/logout (Header: Bearer <Token>)
    AuthCtrl->>TokenBlacklist: blacklistToken(token, remainingTTL)
    TokenBlacklist->>Redis: SET revoked:<token> with TTL
    Redis-->>TokenBlacklist: Stored
    AuthCtrl-->>User: Return 200 OK (Logged out)
```

---

## 💰 Automated Payroll Calculation Engine

```mermaid
flowchart TD
    START(["Initiate Monthly Payroll Calculation"]) --> INPUT["Input: Target Month & Year"]
    INPUT --> FETCH["Fetch all ACTIVE Employees with Department Records"]
    
    FETCH --> LOOP{"Iterate over Employees"}
    LOOP --> BASE["1. Retrieve Base Salary"]
    BASE --> ATTENDANCE["2. Aggregate Attendance & Approved Overtime Hours"]
    ATTENDANCE --> OT_CALC["Calculate Overtime Pay (1.5x Hourly Rate)"]
    OT_CALC --> ALLOWANCE["3. Calculate Allowances (10% Base Salary)"]
    ALLOWANCE --> GROSS["4. Gross Salary = Base + Overtime + Allowances"]
    GROSS --> DEDUCTIONS["5. Compute Deductions (Statutory & Unpaid Absences)"]
    DEDUCTIONS --> TAX["6. Apply Tiered Tax Brackets (10%)"]
    TAX --> NET["7. Net Pay = Gross Salary - Deductions - Tax"]
    
    NET --> PERSIST[("Persist Payroll Record with Status: CALCULATED")]
    PERSIST --> NEXT{"More Employees?"}
    NEXT -- Yes --> LOOP
    NEXT -- No --> SUMMARY["Generate Batch Summary Audit"]
    
    SUMMARY --> APPROVAL{"Admin / HR Review"}
    APPROVAL -- Reject --> RECALC["Adjustment Required"]
    APPROVAL -- Approve --> STATUS_APPROVED["Status: APPROVED"]
    STATUS_APPROVED --> DISBURSE["Corporate Bank Transfer / Payroll Voucher"]
    DISBURSE --> STATUS_PAID["Status: PAID (A4 Statement / CSV Export Ready)"]
    STATUS_PAID --> END(["Workflow Complete"])
```

---

## 🛠️ Technology Stack & Libraries

| Technology | Purpose | Version |
| :--- | :--- | :--- |
| **Java** | High-performance compiled programming language | **25 (LTS)** |
| **Spring Boot** | Application framework & dependency injection engine | **3.5.x** |
| **Spring Security** | Declarative authentication & method-level RBAC | **6.x** |
| **PostgreSQL** | Relational database with ACID transactional integrity | **16-alpine** |
| **Redis** | Distributed in-memory token blacklist & cache | **7-alpine** |
| **JJWT (io.jsonwebtoken)** | JSON Web Token parsing, signing, and verification | **0.12.x** |
| **Spring Data JPA** | Object-Relational Mapping (Hibernate 6) | Spring Boot Starter |
| **Flyway** | Automated database versioning and migrations | **10.x** |
| **Springdoc OpenAPI** | Interactive Swagger UI API documentation | **2.8.x** |
| **Docker & Docker Compose** | Multi-container orchestration & containerization | Multi-stage |

---

## 👥 Role-Based Access Control (RBAC)

The system enforces two strict administrative operational roles:

* **`ADMIN`**: Complete system governance, HR user provisioning, status toggles, salary audits, and full system configuration.
* **`HR`**: Operations management including employee directory, department allocations, daily attendance logs, leave request reviews, and monthly payroll calculation.

> [!NOTE]
> **No self-service employee login role exists.** Employees represent operational master records managed by `ADMIN` and `HR` personnel.

---

## 🚀 Quick Start & Local Development

### Prerequisites
* **Java 25 JDK** installed and active on your environment
* **PostgreSQL 16+** running locally (or via Docker)
* **Redis 7+** (Optional: application gracefully falls back to concurrent memory if Redis is unavailable)

### Running Locally with Maven Wrapper

1. **Clone the repository:**
   ```bash
   git clone https://github.com/YimLemeng/HR-payroll-system.git
   cd HR-payroll-system
   ```

2. **Configure your local properties:**
   Make a copy of `application-local.yml` or configure your local database credentials:
   ```bash
   cp .env.example .env
   ```

3. **Compile and run the Spring Boot application:**
   ```bash
   ./mvnw clean spring-boot:run
   ```

4. **Verify Application Health & Interactive API Docs:**
   * **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
   * **OpenAPI Schema:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
