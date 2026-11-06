# ERP glossary

Canonical terms for the implemented ERP. Definitions are scoped to their owning
module: a sales order, a stock reservation and a shipment describe different
parts of the same workflow. Code identifiers remain in English.

[Domain rules](DOMAIN.md) · [Architecture](ARCHITECTURE.md) · [Security](SECURITY.md)

## Catalog (`catalog`)

| Term / identifier | Definition |
| --- | --- |
| Product — `Product` | A tenant-owned sellable catalog record with a name, SKU, category and base price. It does not hold warehouse stock. |
| Category — `Category` | A tenant-owned product classification, identified by a code unique within the company. |
| SKU — `Sku` | A stable product code, unique within a company. Creation accepts a manual code or generates one from the category code and a company-wide counter. Changing the product category does not change its SKU. |
| Base price — `Product.basePrice` | The catalog monetary price used when creating sales items. A sales item captures its price rather than continuously reading the current catalog price. |

## Companies (`company`)

| Term / identifier | Definition |
| --- | --- |
| Company — `Company` | A business organization identified by a CNPJ and business number. Its ID scopes tenant-owned business records. |
| Tenant | The company whose business data is being accessed. Company details belong here; membership and authorization belong to `iam.accesscontrol`. |
| Factory / correspondent — `CompanyType` | Company classifications used to determine eligible sellers and buyers in sales workflows. |
| Company contact — `CompanyContact` | A contact record owned by a company. It is distinct from a global user and from a customer-module contact person. |
| Company address | Company location information used by business workflows, represented with shared address value objects. |

## Customers and suppliers (`customer`)

| Term / identifier | Definition |
| --- | --- |
| Customer — `Customer` | A buyer registered within a company, with a document unique within that company. A customer is not a global application user. |
| Supplier — `Supplier` | A tenant-owned supplier record. Its existence does not imply a purchasing-order workflow. |
| Contact person — `ContactPerson` | A named contact associated with a customer. It does not provide authentication or tenant membership. |

## Sales (`sales`)

| Term / identifier | Definition |
| --- | --- |
| Sales order — `SalesOrder` | The commercial order recording the seller, buyer, items, payment method and fulfillment choice. It owns commercial confirmation and cancellation. |
| Sales order item — `SalesOrderItem` | A product and warehouse reference, positive quantity, and captured product SKU, name and unit price. |
| Sales buyer — `SalesBuyer` | The buyer representation: a company for B2B or a tenant-owned customer for B2C. |
| B2B / B2C — `SaleType` | B2B sells from a factory to a different correspondent company; B2C sells from a factory or correspondent to one of its customers. |
| Sales order status — `SalesOrderStatus` | The commercial lifecycle: `DRAFT`, `CONFIRMED` or `CANCELLED`. This is separate from shipment progress. |
| Payment method — `PaymentMethod` | The payment method selected for a sale. Selecting it does not by itself record a financial payment. |
| Fulfillment — `FulfillmentType` | `PICKUP` or `DELIVERY`. Delivery requires a route whose origin warehouse is used by every sales item. |
| Delivery route — `DeliveryRoute` | The sales order's delivery instructions: route name, origin warehouse and destination description. Shipment execution belongs to logistics. |

## Inventory (`inventory`)

| Term / identifier | Definition |
| --- | --- |
| Warehouse — `Warehouse` | A tenant-owned stock location. Individual shelf/bin locations are not currently modeled. |
| Stock balance — `InventoryItem` | The on-hand and reserved quantities of one product in one warehouse. |
| Available stock | On-hand quantity minus reserved quantity. |
| Stock reservation — `InventoryReservation` | Stock held for a sales order, product and warehouse. Reservation reduces availability without reducing stock on hand. |
| Stock movement — `StockMovement` | A recorded stock entry, exit, adjustment, reservation or release, with its business reason. |
| Reservation consumption | The operation that reduces both reserved and on-hand quantities when a delivery shipment is loaded. |

## Finance (`financial`)

| Term / identifier | Definition |
| --- | --- |
| Invoice — `Invoice` | A billing record with an amount, currency, due date and billing party, optionally linked to a sales order. It is not a fiscal tax document. |
| Billing party — `BillingPartyType` and billing party ID | The company or customer against whom an invoice is issued. |
| Financial obligation — `FinancialObligation` | An amount receivable or payable with a currency, due date and settlement state. Existing sale-confirmation processing creates receivables; the type enum also includes payables. |
| Receivable / payable — `FinancialObligationType` | A receivable is owed to the company; a payable is owed by the company. Neither is an accounting debit/credit posting. |
| Settlement — `FinancialObligation.settle()` | Marking a financial obligation as settled. It is distinct from creating a cash movement or an accounting posting. |
| Payment — `Payment` | A recorded payment associated with sales/billing references, method, amount and status. Recording a payment does not imply that it is `PAID`. |
| Payment transaction — `PaymentTransaction` | A typed payment-related transaction with an amount, timestamp and optional provider reference. It is not a database transaction. |
| Financial account — `FinancialAccount` | A cash, bank or payment account against which cash movements are recorded. It is distinct from an accounting account. |
| Cash movement — `CashMovement` | A recorded income, expense, transfer or adjustment affecting a financial account. “Cash” includes the supported financial-account types, not only physical currency. |
| Accounting account — `AccountingAccount` | An account in the chart of accounts used by accounting postings. |
| Accounting entry — `AccountingEntry` | A dated debit or credit posting to an accounting account, with amount, currency and source. It is distinct from an obligation or cash movement. |
| Reversal — `AccountingEntry.reverse()` | Reversing a posted accounting entry by creating a posting in the opposite direction. |

These definitions do not add business guarantees. In particular, the current
invoice settlement calculation includes recorded payments regardless of status,
and the current revenue posting is not a complete balanced journal transaction.
See [Domain rules](DOMAIN.md) for those implementation limits.

## Logistics (`logistics`)

| Term / identifier | Definition |
| --- | --- |
| Shipment — `Shipment` | A physical delivery linked to a sales order, with items, route, driver, vehicle and execution state. It retains seller and buyer/customer references but does not own sales pricing. |
| Shipment item — `ShipmentItem` | A product and warehouse reference, captured product SKU/name and quantity for delivery. |
| Driver — `Driver` | The person associated with an application user and assigned to execute deliveries. |
| Vehicle — `Vehicle` | A transport resource assigned to shipment execution. |
| Dispatch assignment | Assigning the driver, vehicle and required route details before loading a shipment. |
| Shipment status — `ShipmentStatus` | The physical execution lifecycle, from creation through assignment, loading, transit and delivery, with cancellation restricted by state. |
| Tracking event — `ShipmentTrackingEvent` | A timestamped record in the shipment's tracking history. |

## Users (`user`)

| Term / identifier | Definition |
| --- | --- |
| User — `User` | A global application identity with profile and contact details. Tenant access is managed separately. |
| User profile | The personal and contact information attached to a global user. It is distinct from credentials and tenant roles. |
| User status — `UserStatus` | The active/inactive state of the global user record. |

## Authentication (`iam`)

| Term / identifier | Definition |
| --- | --- |
| Credential — `IamCredential` | Authentication data provisioned for a global user and used to verify login. |
| Login | The authentication workflow that verifies credentials and issues an access token. |
| JWT | The signed access token issued by IAM. A token is not a user profile or a tenant membership. |

## Authorization (`iam.accesscontrol`)

| Term / identifier | Definition |
| --- | --- |
| Tenant membership — `TenantMembership` | The association between a global user and a company through which tenant access is managed. |
| Tenant role — `TenantRole` | A tenant-owned grouping of permissions assigned through memberships. |
| Permission | An authority required for an operation, such as `financial:read`. It is distinct from the role that groups authorities. |
| Platform role assignment — `PlatformRoleAssignment` | A platform-level role assignment, separate from tenant-owned roles. |
| Authorization audit — `AuthorizationAudit` | A record of an authorization-management action and its actor, target and scope. |

## Executive indicators (`kpi`)

| Term / identifier | Definition |
| --- | --- |
| KPI | An executive indicator calculated for the active tenant from business-module API projections. KPI queries do not own the underlying orders, stock or financial records. |
| Snapshot | A public module projection used as reporting input. The source module remains the owner of the business concept. |
| Reporting period | A UTC date range used to aggregate indicators. |
| Currency grouping | Keeping monetary totals separate by currency rather than adding unlike currencies together. |

## Shared concepts (`common`)

| Term / identifier | Definition |
| --- | --- |
| Money — `Money` | A monetary amount together with its currency. Business pricing and settlement rules remain in their owning modules. |
| Address — `Address` | A shared value object describing a location. The business party using it owns its role in the workflow. |
| Tenant context — `TenantContext` | The active company scope used by tenant-owned business operations. |
| Domain validation | Shared primitives that reject invalid attributes and report domain errors. Aggregate-specific rules remain in their business modules. |
| After commit — `AfterCommit` | The shared mechanism for deferring side effects until a successful transaction commit. |

## Terms not implemented

`PickingOrder` would describe warehouse picking work and `PurchaseOrder` would
describe an order issued to a supplier. Neither exists in this ERP today. There
are no separate WMS, procurement or fiscal modules. A supplier record is not a
purchase order, a shipment is not a picking order, and an invoice is not a fiscal
document.

## Maintaining the vocabulary

Use the canonical identifiers for new code and interfaces. Update this glossary,
the affected module's root `package-info.java`, and its consumers together when
a term changes. Package Javadocs summarize three or four principal terms; this
file provides the broader vocabulary and distinctions.
