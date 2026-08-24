# AGENTS.md

# Repository Guidelines

## Overview

This repository is a Java 21 Spring Boot ERP API built as a **Modular Monolith** using **Spring Modulith**.

Source:

```text
src/main/java/com/thiagsilvadev/erpapi
```

Tests:

```text
src/test/java/com/thiagsilvadev/erpapi
```

Business modules:

- catalog
- company
- customer
- financial
- iam
- inventory
- logistics
- sales
- user

Each module exposes public contracts through `api` and keeps implementation details inside `internal`.

Before completing any task, run:

```bash
./mvnw verify
```

---

# Working Rules

Before making changes:

1. Identify the owning module.
2. Follow existing patterns before introducing new ones.
3. Keep changes local to the affected module.
4. Make the smallest change that satisfies the request.
5. Add or update tests whenever behavior changes.

Do not introduce new frameworks, abstractions, or dependencies unless clearly justified.

---

# Java Imports

Never use fully qualified class names (FQNs) inline in Java source or test code.

Always add an explicit import or static import and refer to the type, method, enum, or constant by its simple name. This rule also applies to annotations, generic type arguments, constructor calls, method references, Mockito calls, and Spring Security expressions such as `@PreAuthorize`.

The only permitted FQNs are in `package` and `import` declarations, or where Java requires an FQN to resolve an unavoidable name collision. In that exceptional case, document the collision next to the usage.

Before completing Java changes, verify that no new inline FQNs were introduced.

---

# Architecture

## Core Principles

- Modular Monolith with Spring Modulith.
- Rich domain model.
- Smart factories, simple constructors.
- Thin controllers.
- Use cases orchestrate workflows.
- Repositories handle persistence only.
- Entities enforce business rules.

---

# Module Boundaries

Spring Modulith boundaries are mandatory.

## Allowed

Cross-module communication only through another module's `api`.

Examples:

```java
CompanySnapshot company = companyFacade.findById(id);
```

```java
inventoryFacade.reserveStock(command);
```

```java
applicationEventPublisher.publishEvent(event);
```

## Forbidden

Never import another module's `internal` package.

Never inject another module's:

- repositories
- entities
- DTOs
- mappers
- services

Never expose JPA entities outside their owning module.

---

# Layer Responsibilities

## Controllers

Responsibilities:

- receive requests
- validate input
- invoke use cases
- return responses

Never:

- implement business rules
- access repositories
- coordinate workflows

---

## Use Cases

Responsibilities:

- orchestrate business workflows
- define transactions
- invoke repositories
- invoke module APIs
- publish events

---

## Repositories

Responsibilities:

- persistence only

Never:

- contain business logic
- orchestrate workflows
- publish events

---

## Entities

Entities own business invariants.

Prefer:

```java
order.confirm();
company.activate();
inventory.reserve(quantity);
```

Avoid:

```java
order.setStatus(CONFIRMED);
```

Avoid public setters.

---

# Domain Modeling

Use smart factory methods.

Example:

```java
SalesOrder.create(...);
```

Constructors should primarily assign values.

Use value objects for concepts such as:

- Money
- Email
- Address
- Phone
- Cnpj

---

# Module Structure

```
module/
├── api
└── internal
```

### api

Public contracts only:

- Facades
- Commands
- Snapshots
- Events
- Enums

### internal

Implementation details.

Typical packages:

```
controller
dto
usecase
model
repository
mapper
```

### Naming

- Facade contracts live in `api` as `XFacade`; their implementations live in `internal/service` as `XFacadeImpl`.
- Never name a facade implementation `XService` — always derive the name from the interface it implements
  (e.g. `CredentialProvisioningFacade` is implemented by `CredentialProvisioningFacadeImpl`, not `CredentialProvisioningService`).
- Plain domain services that do not implement an `api` facade keep the `XService` name.

---

# Persistence

- Keep JPA entities inside `internal/model`.
- Use DTOs at REST boundaries.
- Prefer IDs over cross-module entity relationships.
- Use `FetchType.LAZY`.
- Use `@Enumerated(EnumType.STRING)`.
- Use `@Version` when concurrent updates are possible.

Transactions belong in use cases.

Commands:

```java
@Transactional
```

Queries:

```java
@Transactional(readOnly = true)
```

---

# Mapping

Prefer manual mapping.

Use MapStruct only when it significantly reduces repetitive boilerplate.

Naming:

```text
CustomerMapper
OrderMapper
PaymentMapper
```

Methods:

```java
toEntity(...)
toSnapshot(...)
toResponse(...)
```

---

# Validation & Errors

Validate:

- external input with Bean Validation
- business rules inside the domain model

Use domain-specific exceptions.

Examples:

- ResourceNotFoundException
- ConflictException
- DomainValidationException

Controllers should not translate exceptions manually.

---

# Logging

Use structured logging.

Good:

```java
log.info("Payment processed. paymentId={}", paymentId);
```

Never log:

- passwords
- JWTs
- secrets
- sensitive business data

---

# Shared Code

`common` is only for true cross-cutting concerns such as:

- validation
- security
- exception handling
- generic utilities

Business logic belongs in its owning module.

When unsure, keep code inside the module.

---

# Testing

Use:

- JUnit 5
- AssertJ
- Mockito
- Spring Test
- Spring Modulith Test

Rules:

- add tests when behavior changes
- test business rules
- test failure paths
- keep `ApplicationModularityTests` passing

Prefer deterministic tests.

Avoid time-based sleeps when possible.

Exception assertions should contain only the action expected to fail.

---

# Build

Useful commands:

```bash
docker compose up -d

docker compose --profile lgtm up -d

./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

./mvnw spring-boot:run -Dspring-boot.run.profiles=dev,otel

./mvnw spotless:apply

./mvnw test

./mvnw verify
```

Always finish with:

```bash
./mvnw verify
```

---

# Security

- Never commit secrets.
- Never commit production credentials.
- Document required environment variables.

Development JWT secret:

```text
ERP_IAM_JWT_SECRET
```

---

# Database

No migration tool is currently configured.

Do not add ad hoc SQL scripts.

If migrations become necessary, use Flyway:

```text
src/main/resources/db/migration
```

Never rely on Hibernate auto-DDL in production.

---

# Agent Behavior

Always:

- preserve module boundaries
- preserve architectural patterns
- prefer existing conventions
- keep changes focused
- avoid unnecessary abstractions

When uncertain, follow the existing implementation style of the affected module.
