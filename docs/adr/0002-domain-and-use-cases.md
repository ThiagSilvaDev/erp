# ADR 0002: Domain behavior, use cases and ID references

Status: Accepted (retrospective).

## Context

Sales, stock, billing and shipment operations combine local business rules with
coordination across module owners. Persistence entities must not become the
public integration contract.

## Decision

Place local invariants and transitions in entities/value objects. Use cases
coordinate workflows and normally define their transactions; controllers handle
requests and responses, and repositories handle persistence. Reference other
modules by IDs and obtain their data through API snapshots/contracts.

## Consequences

Business rules can be exercised without HTTP or a running database. Cross-module
consistency requires explicit API calls or event workflows rather than JPA
entity navigation. This is the repository convention: some current constructors
and domain services also validate inputs, and services/facades/listeners may
have their own transaction annotations.

## References

[Domain](../DOMAIN.md),
[confirmation use case](../../apps/api/src/main/java/com/thiagsilvadev/erpapi/sales/internal/usecase/ConfirmSalesOrderUseCase.java),
[SalesOrder](../../apps/api/src/main/java/com/thiagsilvadev/erpapi/sales/internal/model/SalesOrder.java),
[agent instructions](../../apps/api/AGENTS.md).
