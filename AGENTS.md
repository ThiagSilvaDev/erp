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
- kpi
- logistics
- sales
- user

Each module exposes public contracts through `api` and keeps implementation details inside `internal`.

Supporting documentation:

- [Architecture](docs/ARCHITECTURE.md): structure, module dependencies and workflows.
- [Domain](docs/DOMAIN.md): implemented ERP concepts and business rules.
- [Security](docs/SECURITY.md): authentication, authorization and token lifecycle.
- [Testing](docs/TESTING.md): test strategy, conventions and prerequisites.
- [ADRs](docs/adr/README.md): architectural decisions and consequences.

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

## Commit Messages

Use Conventional Commit messages that match the repository history:

```text
<type>(<scope>): <short imperative summary>
```

Use a lowercase type and scope, such as `feat(company)`, `fix(financial)`,
`refactor(validation)`, or `chore(compose)`. Choose a scope that names the
affected module or component. Omit the scope only for changes that do not fit a
single area. Keep the summary concise, start it with an imperative verb, and do
not end it with a period.

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

Read operations for one aggregate live in a single `<Aggregate>QueryUseCase` exposing `getById(...)` and
`list(...)`; each method keeps its own `@Transactional(readOnly = true)` and `@PreAuthorize`. One class per
aggregate — never one class per module; command workflows keep one class per operation.

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
- A facade implementation is a thin adapter: it delegates to an internal service that owns the workflow,
  and it never exposes public methods that are missing from the facade contract.
- Use cases depend on internal services, never on `XFacadeImpl` classes.

### Mapper package

All mappers live in `internal/mapper`. Nothing that maps entities to DTOs belongs in `internal/dto`
or in `internal/usecase`.

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

### After-commit side effects

Work that must not run for rolled-back transactions (credential provisioning, event publication) goes
through `common/transaction/AfterCommit.run(...)`. Do not register `TransactionSynchronization` by hand.

Never move secrets such as raw passwords onto the application event bus: the Modulith event registry
can persist events that have transactional listeners.

### Unique constraint races

Concurrent creations are guarded by `existsBy...` pre-checks plus the database unique constraint.
The `DataIntegrityViolationException` catch must rethrow a domain exception: swallowing it leaves the
transaction rollback-only and the commit fails with `UnexpectedRollbackException` instead of returning
the translated conflict.

---

# Mapping

Mappers live in `internal/mapper`.

Choose the manual mapping style by size, not by preference:

| Shape | Use |
| --- | --- |
| Up to two flat target records, no embedded sources, no computed fields | Plain class with static methods |
| Three or more targets, embedded sources, or computed/derived fields | MapStruct `@Mapper` |

A type that resolves other data to build a response is not a mapper: it injects a facade or a
repository, is named `XResponseAssembler`, and lives in `internal/service`. Assemblers must not be
used to map collections without batching the lookups.

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

For required-attribute validation inside entities and value objects:

- non-blank text → `Strings.required(...)` / `Strings.normalizeCode(...)`
- non-null references → `DomainAssert.requireNotNull(value, "Attribute name")`

Both throw `InvalidAttributeException`. Pass the attribute name only: the exception appends the reason.
Never use `Objects.requireNonNull` in `internal/model` packages.

`DomainValidationException` and its message-based `DomainAssert` helpers exist for shared value objects,
which cannot own module error codes. Aggregates that own an error-code contract use the `DomainErrorCode`
overloads instead.

### Authorization expressions

Guard use cases with permission constants, never with SpEL type references:

```java
import static com.thiagsilvadev.erpapi.catalog.api.CatalogPermissions.WRITE;

@PreAuthorize("hasAuthority('" + WRITE + "')")
```

Never write `@PreAuthorize("hasAuthority(T(com.example.Permissions).READ)")`. The concatenated constant is
resolved at compile time, keeps the expression free of fully qualified names, and survives renames.
When a permission belongs to another module that already depends on this one, declare a local constant
with a comment explaining the avoided module cycle.

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
- keep `ApplicationModularityTests`, `ArchitectureRulesTest` and `SourceConventionsTest` passing
- test persistence and transaction semantics against PostgreSQL (`dbtest` profile) instead of mocking the
  repository, e.g. `UserEmailUniquenessPersistenceTest`

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
