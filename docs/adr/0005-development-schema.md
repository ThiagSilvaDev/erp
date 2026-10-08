# ADR 0005: Development schema updates and deferred Flyway adoption

Status: Accepted (retrospective).

## Context

Development and database tests currently create/update their schemas through
Hibernate. The repository has no configured migration tool or production schema
rollout process.

## Decision

Retain `ddl-auto=update` for development and the isolated database-test schema.
Defer adding Flyway to the current build. When versioned migrations become
necessary, use Flyway with files under `src/main/resources/db/migration`, as
specified in the repository guidance. Production must use an explicit schema
management process instead of relying on development auto-DDL.

## Consequences

Local schema changes remain convenient but are not a reproducible migration
history. Development/test settings do not establish production readiness.
Adopting Flyway later requires both a baseline for fresh databases and a safe
adoption procedure for existing non-empty schemas; introducing a migration file
alone does not provide that upgrade path.

## References

[Development configuration](../../src/main/resources/application-dev.properties),
[database-test configuration](../../src/test/resources/application-dbtest.properties),
[build dependencies](../../pom.xml),
[database guidance](../../AGENTS.md#database).
