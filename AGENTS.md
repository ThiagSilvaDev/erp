# Repository Guidelines

## Project Overview

This is a Java 21 Spring Boot ERP API organized as a Modular Monolith using Spring Modulith.

Main code lives in:

```text
src/main/java/com/thiagsilvadev/erpapi
```

Tests mirror the source structure:

```text
src/test/java/com/thiagsilvadev/erpapi
```

Business modules include:

* catalog
* company
* customer
* financial
* iam
* inventory
* logistics
* sales
* user

Each module exposes public contracts through `api` and keeps implementation details inside `internal`.

Always run:

```bash
./mvnw verify
```

before finishing changes.

---

## Working Rules

* Always understand the owning module before making changes.
* Keep changes local to the affected module whenever possible.
* Preserve existing naming conventions and architectural patterns.
* Prefer minimal, targeted edits over broad rewrites.
* Do not introduce new frameworks, abstractions, or dependencies unless clearly justified.
* If a requested change violates module boundaries, explain the issue and propose a Modulith-compatible solution.

---

# Architecture

## Core Principles

* Modular Monolith using Spring Modulith.
* Rich Domain Model.
* Smart factories and dumb constructors.
* Thin controllers.
* Use cases to orchestrate business workflows.
* Repositories are persistence-only.
* Entities protect business invariants.

## Layer Responsibilities

### Controllers

* Accept and validate external requests.
* Convert requests into use case invocations.
* Never contain business logic.
* Never access repositories directly.

### Use Cases

* Orchestrate business workflows.
* Define transaction boundaries.
* Coordinate repositories, facades, domain entities, and events.

### Repositories

* Persistence only.
* No orchestration.
* No business rules.

### Entities

* Own business invariants.
* Expose behavior instead of setters.
* Prevent invalid state transitions.

---

## Module Boundaries

* Respect Spring Modulith boundaries strictly.
* Cross-module communication is allowed only through another module's `api` package.
* Never import another module's `internal` classes.
* Never inject another module's repositories, services, entities, DTOs, or mappers.
* Never expose JPA entities outside the owning module.

### Preferred Communication

Reads:

```java
CompanySnapshot company = companyFacade.findById(companyId);
```

Immediate writes:

```java
inventoryFacade.reserveStock(command);
```

Eventual consistency:

```java
applicationEventPublisher.publishEvent(event);
```

### Avoid

```java
@Autowired
private CompanyRepository repository;
```

across module boundaries.

---

## Domain Model

### Entities

* Prefer rich domain models over anemic entities.
* Avoid public setters.
* Use intention-revealing methods.

Good:

```java
order.confirm();
inventory.reserve(quantity);
company.activate();
```

Avoid:

```java
order.setStatus(CONFIRMED);
```

### Construction

Use smart factories and dumb constructors.

Good:

```java
SalesOrder.create(...);
InventoryItem.create(...);
```

Constructors should perform assignments only.

### Value Objects

Use value objects for domain concepts such as:

* Money
* Address
* Email
* Phone
* Cnpj
* GeoLocation

Domain-specific value objects belong to their owning module.

---

## Module Structure

Each module should follow:

```text
<module>/
├── api
└── internal
```

### api

Public contracts only:

* Facades
* Commands
* Snapshots
* Events
* Enums

### internal

Implementation details:

```text
internal/controller
internal/dto
internal/usecase
internal/model
internal/repository
internal/mapper
```

Additional packages are allowed when justified:

```text
internal/domain
internal/model/domain
```

Move to feature-based packaging when folders start containing multiple unrelated flows.

---

## Shared Code

Use `common` only for true cross-cutting concerns.

Allowed examples:

* Validation helpers
* Shared exception handling
* Security primitives
* Generic value objects
* Utilities reused by multiple modules

Domain-specific code belongs in the owning module.

When unsure, keep code inside the owning module.

---

## Persistence

### General

* Keep JPA entities inside `internal/model`.
* REST controllers must use request/response DTOs.
* Prefer IDs instead of JPA relationships across modules.
* Use plural table names.
* Use snake_case column names.

### JPA

* Prefer `FetchType.LAZY`.
* Use `@Enumerated(EnumType.STRING)`.
* Use `@Version` for optimistic locking where concurrent updates are possible.
* Avoid public setters.
* Keep transaction boundaries outside entities.

### Transactions

Use transactions in use cases or application services.

Commands:

```java
@Transactional
```

Queries:

```java
@Transactional(readOnly = true)
```

Repositories and entities must not define transaction boundaries.

---

## Mapping

Prefer manual mappers by default.

Use MapStruct only when generated code clearly reduces maintenance and repetitive boilerplate.

Mapper naming:

```text
CustomerMapper
SalesOrderMapper
PaymentMapper
```

Use clear method names:

```java
toResponse(...)
toSnapshot(...)
toEntity(...)
```

---

## Validation, Errors, and Logging

### Validation

* Validate external input at REST boundaries using Bean Validation.
* Validate business invariants inside the domain model.

### Exceptions

Use domain-specific exceptions for business rule violations.

Common exceptions:

* ResourceNotFoundException
* ConflictException
* DomainValidationException

REST exception translation belongs in global handlers, never controllers.

### Logging

* Never log secrets.
* Never log passwords.
* Never log tokens.
* Never log sensitive business data.
* Prefer structured logging.
* Use placeholders instead of string concatenation.

Good:

```java
log.info("Payment processed. paymentId={}", paymentId);
```

---

## Database Migrations

No migration tool is currently configured.

Do not introduce ad hoc SQL files.

If migrations become necessary:

* Introduce Flyway.
* Store migrations in:

```text
src/main/resources/db/migration
```

Examples:

```text
V1__create_sales_tables.sql
V2__add_customer_status.sql
```

Never rely on Hibernate auto-DDL in production.

---

## Build, Test, and Development Commands

```bash
docker compose up -d
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
./mvnw spotless:apply
./mvnw -q test
./mvnw verify
```

---

## Testing Guidelines

Use:

* JUnit 5
* AssertJ
* Mockito
* Spring Test
* Spring Modulith Test

### General Rules

* Add tests whenever behavior changes.
* Test business rules.
* Test failure paths.
* Add integration tests when module interactions change.
* Keep `ApplicationModularityTests` passing.

### Exception Assertions

Prepare all data before the assertion.

The exception lambda should contain only the action expected to fail.

Good:

```java
var exception = catchThrowableOfType(
        DomainValidationException.class,
        () -> salesOrder.confirm()
);
```

Avoid:

```java
assertThatThrownBy(() ->
        SalesOrder.create(
                UUID.randomUUID(),
                UUID.randomUUID()))
```

---

## Commit and Pull Request Guidelines

Use Conventional Commits.

Examples:

```text
feat(logistics): add shipment workflow
fix(financial): prevent duplicate payment processing
refactor(company): extract address value object
```

---

## Security and Configuration

* Never commit secrets.
* Never commit production credentials.
* Keep environment-specific configuration outside source control.
* Document required environment variables.

Local JWT development uses:

```text
ERP_IAM_JWT_SECRET
```

Production environments must provide secure values externally.

---

## Agent Behavior

* Prefer minimal, focused changes.
* Preserve package structure.
* Preserve module boundaries.
* Preserve architectural patterns.
* Avoid introducing unnecessary abstractions.
* Avoid moving code across modules without justification.
* When uncertain, follow existing patterns in the affected module.
