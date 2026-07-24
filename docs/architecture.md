# Architecture Details

This document complements `AGENTS.md` with additional conventions and examples
that are not covered there but are enforced across the codebase.

---

## Module Boundaries — Anti-Patterns

The following cross-module injection is forbidden:

```java
@Autowired
private CompanyRepository repository;
```

Always go through the owning module's `api` facade instead.

---

## Domain Modeling

### Value Objects

In addition to `Money`, `Email`, `Address`, `Phone`, and `Cnpj`, the codebase
may use:

- `GeoLocation`

Domain-specific value objects belong to their owning module and must not be
shared through `common` unless genuinely cross-cutting.

### Construction

Prefer smart factories and simple constructors.

```java
SalesOrder.create(...);
InventoryItem.create(...);
```

Constructors should perform assignments only, never business rule validation
that belongs in the factory method.

---

## Module Structure

### Allowed Package Extensions

Additional packages are allowed when justified:

```text
internal/domain
internal/model/domain
```

Move to feature-based packaging when folders start containing multiple
unrelated flows.

---

## Persistence

### General Conventions

- Use plural table names.
- Use `snake_case` column names.
- REST controllers must use request/response DTOs, never JPA entities.
- Prefer IDs instead of JPA relationships across modules.

### JPA

- Avoid public setters on entities.
- Keep transaction boundaries outside entities.
- Repositories and entities must not define transaction boundaries.

---

## Mapping

### Method Naming

Use clear, intention-revealing method names:

```java
toResponse(...)
toSnapshot(...)
toEntity(...)
```

Mapper class naming follows the entity concept:

```text
CustomerMapper
SalesOrderMapper
PaymentMapper
```

---

## Validation, Errors, and Logging

### Exceptions

- REST exception translation belongs in global handlers, never controllers.
- Controllers should not translate exceptions manually.

### Logging

- Use placeholders instead of string concatenation.

Good:

```java
log.info("Payment processed. paymentId={}", paymentId);
```

Avoid:

```java
log.info("Payment processed. paymentId=" + paymentId);
```

---

## Database Migrations

If migrations become necessary, introduce Flyway and store migrations in:

```text
src/main/resources/db/migration
```

Example file names:

```text
V1__create_sales_tables.sql
V2__add_customer_status.sql
```

Never rely on Hibernate auto-DDL in production.

---

## Testing Guidelines

### Integration Tests

Add integration tests when module interactions change.

### Exception Assertions

Prepare all data before the assertion. The exception lambda should contain
only the action expected to fail.

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

- Keep environment-specific configuration outside source control.
- Production environments must provide secure values externally.