/**
 * Owns sales orders, their commercial items, buyers and fulfillment choices.
 *
 * <dl>
 *   <dt>SalesOrder
 *   <dd>A commercial order that captures buyer, items and payment method.
 *   <dt>SalesOrderItem
 *   <dd>A product, quantity and captured SKU, name and unit price.
 *   <dt>SalesBuyer
 *   <dd>The buyer company for B2B or tenant customer for B2C.
 *   <dt>SalesOrderStatus
 *   <dd>The draft, confirmed or cancelled state of a sales order.
 * </dl>
 *
 * <p>Canonical definitions are maintained in {@code docs/GLOSSARY.md}.
 */
@ApplicationModule(
        allowedDependencies = {
            "common::model",
            "common::exception",
            "common::numbering",
            "common::permission",
            "common::reporting",
            "common::tenant",
            "common::validation",
            "common::web",
            "catalog::api",
            "company::api",
            "customer::api",
            "inventory::api"
        })
package com.thiagsilvadev.erpapi.sales;

import org.springframework.modulith.ApplicationModule;
