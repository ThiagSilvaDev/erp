# ADR 0004: Synchronous stock coordination and persisted events

Status: Accepted (retrospective).

## Context

A sale must reserve stock before its confirmation transaction completes.
Invoicing and shipment creation belong to different modules and are triggered
by sales events.

## Decision

Reserve inventory through its public facade in the confirmation workflow.
Publish confirmation and delivery events using Spring Modulith's JDBC event
publication integration. Financial and logistics `@ApplicationModuleListener`
handlers process those events asynchronously after commit in their own
transactions.

## Consequences

Stock failure prevents confirmation from completing. Invoices and shipments are
eventually available and may appear after the API response. Creation handlers
check existing sales-order references; persisted publications do not establish
exactly-once delivery or a universal automatic-retry policy. Cancellation
listener failures do not undo an already committed sales cancellation, so
outstanding publication failures need operational attention.

## References

[Confirmation flow](../ARCHITECTURE.md#confirming-a-sale),
[confirmation use case](../../apps/api/src/main/java/com/thiagsilvadev/erpapi/sales/internal/usecase/ConfirmSalesOrderUseCase.java),
[financial listener](../../apps/api/src/main/java/com/thiagsilvadev/erpapi/financial/internal/listener/SalesOrderFinancialListener.java),
[logistics listener](../../apps/api/src/main/java/com/thiagsilvadev/erpapi/logistics/internal/listener/SalesOrderLogisticsListener.java),
[dependencies](../../apps/api/pom.xml).
