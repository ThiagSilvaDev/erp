/**
 * Owns shipment execution, drivers, vehicles and tracking.
 *
 * <dl>
 *   <dt>Shipment
 *   <dd>A physical delivery linked to a sales order, retaining buyer references.
 *   <dt>Driver
 *   <dd>The user-associated person assigned to execute a delivery.
 *   <dt>Vehicle
 *   <dd>The transport resource assigned to a shipment.
 *   <dt>ShipmentTrackingEvent
 *   <dd>A timestamped record in the shipment tracking history.
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
            "company::api",
            "inventory::api",
            "sales::api",
            "user::api"
        })
package com.thiagsilvadev.erpapi.logistics;

import org.springframework.modulith.ApplicationModule;
