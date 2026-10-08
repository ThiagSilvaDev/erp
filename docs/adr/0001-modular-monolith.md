# ADR 0001: Modular monolith and public module contracts

Status: Accepted (retrospective).

## Context

The application contains several business capabilities deployed together. Module
ownership must remain visible even though code and persistence share one runtime.

## Decision

Keep one Spring Boot application organized into Spring Modulith business modules.
Expose cross-module contracts through named `api` interfaces and use specific
named interfaces for `common`. Keep implementation packages internal and verify
boundaries with Spring Modulith.

## Consequences

Deployment and local development use one application and database. Internal
module implementation remains replaceable behind public contracts. Sharing a
runtime does not permit direct access to another module's repositories or
entities, and the current system does not provide independent module deployment.

## References

[Architecture](../ARCHITECTURE.md),
[modularity verification](../../src/test/java/com/thiagsilvadev/erpapi/ApplicationModularityTests.java),
[module declarations](../../src/main/java/com/thiagsilvadev/erpapi/sales/package-info.java).
