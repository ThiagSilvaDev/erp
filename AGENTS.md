# Repository Guidelines

## Project Structure
Java 21 Spring Boot ERP API organized with Spring Modulith. Main code lives in
`src/main/java/com/thiagsilvadev/erpapi`; tests mirror it in `src/test/java`. Modules include `catalog`,
`company`, `customer`, `financial`, `iam`, `inventory`, `logistics`, `sales`, and `user`. Each module
exposes contracts from `api` and keeps implementation in `internal`. Bruno requests live in
`bruno/erp-api`.

Always run `./mvnw verify` before finishing code changes.

## Architecture Rules
- Communicate across modules only through another module's `api` package.
- Never import another module's `internal` classes, repositories, entities, DTOs, mappers, or services.
- Keep JPA entities inside `internal`; REST controllers must use request/response DTOs.
- Use facades/snapshots for reads, events for eventual consistency, and `api` commands only for immediate
  consistency.
- Use `common` only for validation, exception handling, security primitives, generic value objects, and
  utilities reused by 3+ modules. Domain-specific value objects stay in their owning module. When unsure,
  keep code in the owning module and ask before adding shared abstractions.

## Packaging
Use `internal/controller`, `internal/dto`, `internal/usecase`, `internal/model`, `internal/repository`, and
`internal/mapper`. `internal/model` is for JPA entities; place non-persistent domain objects in
`internal/domain` or `internal/model/domain`. Move to feature packages when a folder contains multiple
unrelated use cases.

## Coding Style
Use constructor injection and keep transactions at the use case layer. Prefer records for immutable DTOs
and snapshots. Apply Palantir Java Format with Spotless. Prefer manual mappers; use MapStruct only when
generated code clearly reduces maintenance.

## Validation, Errors, and Logging
Validate external input at REST boundaries with Bean Validation. Use domain exceptions for business rules.
Use `ResourceNotFoundException`, `ConflictException`, and `DomainValidationException` for generic cases.
REST error translation belongs in global handlers under `common/web/exception`, not controllers. Never log
secrets, tokens, passwords, or sensitive business data.

## Database Migrations
No migration tool is configured yet. Do not add ad hoc SQL files. If a schema change requires migrations,
introduce Flyway and use `src/main/resources/db/migration/V<version>__<description>.sql`, for example
`V1__create_logistics_tables.sql`. Decide repeatable migrations, baseline behavior, and test-data strategy
when Flyway is introduced. Do not rely on Hibernate auto-DDL in production.

## Build, Test, and Development Commands
- `docker compose up -d`: starts local PostgreSQL.
- `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`: runs the API locally.
- `./mvnw spotless:apply`: formats Java and POMs.
- `./mvnw -q test`: runs the test suite with quieter output.
- `./mvnw verify`: runs compilation, tests, packaging, Modulith checks, and Spotless validation.

## Testing Guidelines
Use JUnit 5, AssertJ, Mockito, Spring test support, and Spring Modulith tests. Add focused tests for changed
use cases, domain rules, security, and listeners. Add integration tests for facade/event boundaries when
module interactions change. `ApplicationModularityTests` must keep passing.

## Commit and Pull Request Guidelines
Use Conventional Commits, for example `feat(logistics): add shipment workflow` or
`fix(financial): prevent duplicate payment processing`. PRs should describe behavior changes, test results,
and Bruno updates when API endpoints change.

## Security and Configuration
Never commit real secrets. Local JWT development uses `ERP_IAM_JWT_SECRET`; real environments must provide
a secure value outside source control.
