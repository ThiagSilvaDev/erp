/**
 * Owns warehouses, stock balances, stock reservations and movements.
 *
 * <dl>
 *   <dt>Warehouse
 *   <dd>A tenant-owned stock location.
 *   <dt>InventoryItem
 *   <dd>The stock balance for one product in one warehouse.
 *   <dt>InventoryReservation
 *   <dd>Stock held for a sales order without reducing on-hand quantity.
 *   <dt>StockMovement
 *   <dd>A recorded stock entry, exit, adjustment, reservation or release.
 * </dl>
 *
 * <p>Canonical definitions are maintained in {@code docs/GLOSSARY.md}.
 */
@ApplicationModule(
        allowedDependencies = {
            "common::exception",
            "common::model",
            "common::validation",
            "common::numbering",
            "common::permission",
            "common::reporting",
            "common::tenant",
            "common::web",
            "catalog::api",
            "company::api"
        })
package com.thiagsilvadev.erpapi.inventory;

import org.springframework.modulith.ApplicationModule;
