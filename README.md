# Personal Finance & Subscription Tracker

## 1. Brief Description

Personal Finance & Subscription Tracker is a backend-first Spring Boot application that helps users manage:
1. recurring subscriptions,
2. one-time expenses,
3. category/budget limits,
4. notifications for renewals and budget thresholds,
5. financial analytics reports (monthly, annual, optimization).

The system is designed as a REST API under `/api/v1/**`, using layered architecture and domain-driven modeling for billing cycles, budget thresholds, and notification rules.

## 2. Tech Stack

| Area | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.2.4 |
| API Layer | Spring Web (REST controllers) |
| Persistence | Spring Data JPA + Hibernate |
| Database | H2 in-memory (development) |
| Validation | Jakarta Bean Validation |
| Security | Spring Security (dev-permissive API/H2 setup) |
| Build Tool | Maven |
| Containerization | Docker + Docker Compose |
| View / Frontend | JavaFX Desktop Client (MVC-style UI + API client layer) |

## 3. Instructions to Run

### Prerequisites

1. Java 17+
2. Maven 3.9+
3. Docker Desktop / Docker Engine + Docker Compose

### Install dependencies / build

```bash
mvn clean install -DskipTests
```

### Run locally

```bash
mvn spring-boot:run
```

App URL: `http://localhost:8080`  
API base: `http://localhost:8080/api/v1`

### Run JavaFX desktop frontend

Start backend first, then in a second terminal run:

```bash
mvn javafx:run
```

The JavaFX client opens with a dark, high-contrast theme and tabs that cover all backend modules:
auth/users, payments, subscriptions, expenses, budgets, categories, notifications, and reports/export.

### Run with Docker

```bash
docker compose up --build
```

Detached mode:

```bash
docker compose up --build -d
```

Stop:

```bash
docker compose down
```

### H2 Console

URL: `http://localhost:8080/h2-console`  
JDBC URL: `jdbc:h2:mem:financedb`  
Username: `sa`  
Password: *(blank)*

## 4. Project Structure Tree + What Each Part Does

```text
.
├── Dockerfile
├── docker-compose.yml
├── pom.xml
├── README.md
└── src
    ├── main
    │   ├── java/com/finance/tracker
    │   │   ├── FinanceTrackerApplication.java
    │   │   ├── config/
    │   │   ├── controller/
    │   │   ├── dto/
    │   │   ├── exception/
    │   │   ├── infrastructure/
    │   │   ├── model/
    │   │   ├── repository/
    │   │   └── service/
    │   └── resources/
    └── test/
```

### Root-level files

1. `pom.xml`  
   Declares all dependencies (web, data-jpa, validation, security, H2, testing, PDF export library) and Spring Boot Maven plugin.

2. `Dockerfile`  
   Multi-stage container build:
   - Stage 1: compile/package JAR with Maven
   - Stage 2: run packaged JAR on JRE image

3. `docker-compose.yml`  
   Defines a single service for the backend, image build context, container name, restart policy, and port mapping `8080:8080`.

### Source packages

1. `config/`  
   Cross-cutting app configuration:
   - CORS behavior
   - security filter chain behavior for development flows

2. `controller/`  
   HTTP entry points only (no heavy business logic).  
   They:
   - validate request payloads
   - map request/response shapes
   - delegate operations to services

3. `dto/`  
   Request payload contracts for create/update operations.  
   Keeps controller contracts explicit and validation-focused.

4. `exception/`  
   Custom exception types + global exception mapping to HTTP responses.

5. `infrastructure/`  
   External/technical adapters:
   - notification sender adapters
   - report exporter adapters (CSV/PDF)

6. `model/`  
   Domain objects:
   - entities
   - enums
   - payment inheritance hierarchy
   - reporting interface abstraction

7. `repository/`  
   Spring Data JPA repositories with derived query methods.

8. `service/`  
   Core business layer:
   - transactions
   - orchestration
   - business rules and policy decisions

## 5. Overview of Features + Modules (Expanded)

### 5.1 User & Authentication Module

1. Register, profile update, fetch, and delete users.
2. Lightweight login/logout endpoints (currently simplified auth flow).
3. Default notification preferences seeded when absent.
4. User deletion cascades to owned child records (subscriptions, expenses, budgets, notifications, payment methods).

### 5.2 Payment Method Module

1. Supports polymorphic payment methods:
   - Credit Card
   - Bank Account
   - Digital Wallet
2. Uses JOINED inheritance and Jackson type metadata for REST deserialization.
3. Sensitive values are masked before persistence.
4. User-scoped add/list/delete and payment-process validation endpoint.

### 5.3 Subscription Module

1. User-scoped subscription creation and listing.
2. Embedded `BillingCycle` value object computes:
   - next payment date
   - days until payment
   - projected annual cost
3. Supports status transitions and full subscription updates (`PUT`).
4. Integrates with category and payment method ownership rules.

### 5.4 Expense Module

1. Log one-time expenses with category + payment method linkage.
2. Fetch list and single expense by id.
3. Delete expense with rollback of category spend totals.
4. On create/delete, updates matching user budgets where applicable.

### 5.5 Budget Module

1. Create/list/update budgets per user.
2. Tracks `spendingLimit`, `currentSpending`, period, and remaining amount.
3. Domain methods expose:
   - exceeded state
   - threshold checks (`checkThreshold`)
4. Integrated into expense flow + scheduler checks.

### 5.6 Category Module

1. List categories by user.
2. Patch category budget limit for existing categories.
3. Category-level spending used in notifications and report breakdowns.

### 5.7 Notification Module

1. Types:
   - renewal reminders
   - budget warning
   - budget exceeded
2. Duplicate suppression based on `(user, type, referenceId, unread)` criteria.
3. Preference-aware behavior (can store silently dismissed notifications).
4. Endpoints for list + mark-as-read.
5. Scheduler and expense flows both feed notification creation.

### 5.8 Scheduler Module

1. Daily scheduled check (`@Scheduled` fixed-rate).
2. Subscription fork:
   - checks active subscriptions near renewal threshold
   - emits renewal reminders with date and amount context
3. Budget fork:
   - scans budgets for 80%/100% threshold crossings
   - performs duplicate-safe notification emission

### 5.9 Reporting Module

1. Monthly report:
   - category breakdown
   - subscription total
   - expense total
   - payment-method grouping (null-safe)
2. Annual report:
   - 12 monthly snapshots
   - top-level summary:
     - `totalAnnualExpenses`
     - `totalAnnualSubscriptions`
     - `grandTotal`
3. Optimization report:
   - top expenses
   - unused subscriptions with explicit `considerCancelling: true`
4. Optional category filters across monthly/annual/optimization endpoints.
5. Export as CSV or PDF.

## 6. Design Principles Used (SOLID + GRASP) — Specifics

### SOLID

1. **Single Responsibility Principle (SRP)**  
   - Controllers: HTTP handling only  
   - Services: business workflows and transaction boundaries  
   - Repositories: persistence concerns  
   - Entities: domain state + select domain behavior

2. **Open/Closed Principle (OCP)**  
   Export and sender abstractions allow new formats/channels with minimal change to orchestration logic.

3. **Liskov Substitution Principle (LSP)**  
   `PaymentMethod` subtypes are substitutable in service and persistence flows.

4. **Interface Segregation Principle (ISP)**  
   Narrow adapter interfaces (`IReportExporter`, `INotificationSender`) avoid oversized contracts.

5. **Dependency Inversion Principle (DIP)**  
   Business services depend on abstractions/repository contracts and not concrete infrastructure details.

### GRASP

1. **Controller**  
   REST controllers coordinate use cases and delegate work.

2. **Information Expert**  
   `BillingCycle` and `Budget` own threshold/date calculations.

3. **Pure Fabrication**  
   Services encapsulate orchestration that doesn’t belong directly in entities/controllers.

4. **Low Coupling / High Cohesion**  
   Module boundaries (expense/report/notification/etc.) keep classes focused and interactions explicit.

## 7. Design Patterns Used (Creational + Structural) — Specifics

### Creational Patterns

1. **Builder Pattern**  
   `@Builder` is used for clean construction of entities and request transformations.

2. **Polymorphic Type Construction (JSON discriminator-based)**  
   While not a classical factory class, incoming polymorphic payloads are materialized into concrete `PaymentMethod` subtype instances via Jackson type info.

### Structural Patterns

1. **Adapter Pattern**  
   - `INotificationSender` + implementations adapt notification dispatch channel behavior.
   - `IReportExporter` + CSV/PDF implementations adapt output format behavior.

2. **Facade-like Service Layer**  
   Services expose stable use-case operations while internally coordinating multiple repositories/entities.

3. **Layered Architecture Pattern**  
   Clear separation across API, service, domain, repository, and infrastructure boundaries.

## 8. Anti-Patterns Avoided (Specifics)

1. **God Controller / Fat Controller** avoided  
   Controllers do not own business orchestration.

2. **Leaky Layering** avoided  
   Repository access is routed through services (not directly from most controllers).

3. **Copy-paste threshold logic sprawl** mitigated  
   Domain threshold behavior is centralized in `Budget.checkThreshold(...)`.

4. **Tight coupling to transport/infrastructure** avoided  
   Services depend on interfaces and adapters for export/notification channels.

5. **Silent error masking** avoided  
   Errors are surfaced through typed exceptions and global exception handling.

6. **Null-unsafe analytics grouping** addressed  
   Reporting logic uses null-safe payment grouping keys.
