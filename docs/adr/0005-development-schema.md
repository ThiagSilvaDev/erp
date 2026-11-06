# ADR 0005: Disposable development databases and schema recreation

Status: Accepted; updated for the current development-only database policy.

## Context

Development and database tests currently create/update their schemas through
Hibernate. The repository has no configured migration tool or production schema
rollout process.

## Decision

Retain `ddl-auto=update` for development and the isolated database-test schema.
The project is in development with no near-term production rollout planned.
Local development and test data are disposable. Recreate the project database
when schema changes require it, rather than introducing migrations, Flyway or
compatibility code to preserve existing local data. Limit resets to this
project's local database and leave unrelated databases and shared volumes intact.

Revisit migrations and data preservation only when production planning is
explicitly introduced. Production must use an explicit schema-management process
instead of relying on development auto-DDL.

## Consequences

Local schema changes remain convenient but are not a reproducible migration
history. Development/test settings do not establish production readiness.
Database recreation removes local data and is an intentional development
operation. There is no supported upgrade path for existing local schemas.

The original decision deferred Flyway while describing its eventual adoption.
The current decision explicitly favors database recreation during development;
it does not select a future production migration tool.

## References

[Development configuration](../../apps/api/src/main/resources/application-dev.properties),
[database-test configuration](../../apps/api/src/test/resources/application-dbtest.properties),
[build dependencies](../../apps/api/pom.xml),
[database guidance](../../apps/api/AGENTS.md#database).
