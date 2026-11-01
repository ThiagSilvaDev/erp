# Testing

[Architecture](ARCHITECTURE.md) · [Domain](DOMAIN.md) ·
[Security](SECURITY.md) · [Decisions](adr/README.md)

## Strategy and existing coverage

Tests live under `src/test/java/com/thiagsilvadev/erpapi`, mirroring the owning
module. Use the smallest test that exercises the changed rule and its failure
paths; add integration coverage when persistence or module interactions matter.

| Area | Existing approach | Example |
| --- | --- | --- |
| Domain | JUnit 5 and AssertJ for factories, invariants and transitions | [SalesOrderTest](../src/test/java/com/thiagsilvadev/erpapi/sales/internal/model/SalesOrderTest.java) |
| Use cases/services | Mockito collaborators, arguments and resulting state | [CreateSalesOrderUseCaseTest](../src/test/java/com/thiagsilvadev/erpapi/sales/internal/usecase/CreateSalesOrderUseCaseTest.java) |
| Query use cases | Covered through the controller test driving `getById`/`list`, plus tenant-scope tests | [SalesOrderTenantScopeUseCaseTest](../src/test/java/com/thiagsilvadev/erpapi/sales/internal/usecase/SalesOrderTenantScopeUseCaseTest.java) |
| HTTP | MockMvc request/response and security assertions | [AuthControllerTest](../src/test/java/com/thiagsilvadev/erpapi/iam/internal/controller/AuthControllerTest.java) |
| Persistence | Spring Data JPA tests against PostgreSQL | [AuthorizationAuditJpaTest](../src/test/java/com/thiagsilvadev/erpapi/iam/accesscontrol/internal/model/AuthorizationAuditJpaTest.java) |
| Transactional integration | Spring Boot tests for audit persistence and failure | [AuthorizationAuditListenerPersistenceTest](../src/test/java/com/thiagsilvadev/erpapi/iam/accesscontrol/AuthorizationAuditListenerPersistenceTest.java) |
| Constraint and commit semantics | `@SpringBootTest` with `dbtest` and `TransactionTemplate` against real PostgreSQL | [UserEmailUniquenessPersistenceTest](../src/test/java/com/thiagsilvadev/erpapi/user/internal/repository/UserEmailUniquenessPersistenceTest.java) |
| Architecture | Spring Modulith verification and ArchUnit rules | [ApplicationModularityTests](../src/test/java/com/thiagsilvadev/erpapi/ApplicationModularityTests.java), [ArchitectureRulesTest](../src/test/java/com/thiagsilvadev/erpapi/ArchitectureRulesTest.java) |
| Source conventions | File scan of `src/main/java` for rules invisible in bytecode | [SourceConventionsTest](../src/test/java/com/thiagsilvadev/erpapi/SourceConventionsTest.java) |

Modularity verification checks the module dependency structure. Additional
architecture rules prohibit controller persistence access, controllers
implementing module facades, use cases depending on `XFacadeImpl` classes,
MapStruct mappers outside `internal/mapper`, side effects registered directly
with the transaction synchronization API, and facade contracts exposing
repository-style method names or `Optional` results.

`SourceConventionsTest` covers the conventions that leave no trace in compiled
bytecode: no fully qualified names inline, no `T(...)` SpEL type references in
`@PreAuthorize`, no `Objects.requireNonNull` in `internal/model`, and no mappers
outside `internal/mapper` or mappers that inject facades/repositories.

When a workflow depends on transaction boundaries (unique constraints, after-commit
side effects), assert it against PostgreSQL with an explicit `TransactionTemplate`
instead of mocking the repository; a mocked repository cannot show that the commit
fails with `UnexpectedRollbackException`.

The application smoke/OpenAPI test uses mocked repositories with database
infrastructure excluded. It does not replace tests against real PostgreSQL.
Spring Modulith Test is a dependency; its presence alone does not imply every
module has an isolated `@ApplicationModuleTest` suite.

## Conventions

- Exercise business behavior and observable results, including rejected inputs,
  invalid transitions, tenant isolation and permission failures.
- Prepare fixtures before exception assertions; the assertion lambda contains
  only the action expected to fail.
- Prefer explicit mocks and argument capture where the workflow's side effects
  are the contract. Avoid assertions that merely reproduce implementation steps.
- Inject a fixed `Clock` for time-dependent behavior and use explicit UTC dates
  where required. [FixedClockTestConfiguration](../src/test/java/com/thiagsilvadev/erpapi/support/FixedClockTestConfiguration.java)
  supports Spring tests; unit tests can supply their own fixed clock.
- Prefer deterministic synchronization to time-based sleeps. Keep test data in
  the relevant module and reset shared tenant context when a test sets it.

## Database prerequisites

Start the local PostgreSQL service before the full test suite:

```bash
docker compose up -d
```

Tests using the `dbtest` profile connect to the Compose database with
`currentSchema=erp_test`. The connection initializer creates that schema,
Hibernate updates it, and Modulith initializes its event tables. Development
uses the default schema, so test tables are separated from development tables.
The schemas still share a PostgreSQL instance; this is not a fresh database per
test run or an automatic database-reset guarantee.

Connection details and the test signing secret are defined in
[application-dbtest.properties](../src/test/resources/application-dbtest.properties).
The repository does not configure a Testcontainers-based database lifecycle.
LGTM and the `otel` profile are not prerequisites for verification.

## Commands and completion criteria

```bash
./mvnw test
./mvnw -Dtest=SalesOrderTest test
./mvnw spotless:check
./mvnw verify
```

`verify` runs the tests and the configured Spotless check. Run it before
completing repository work, as required by [AGENTS.md](../AGENTS.md). A successful
build checks the current suite; it is not evidence of complete coverage of every
ERP workflow. New behavior needs tests for the rules and failure paths it adds.
