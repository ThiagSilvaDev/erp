/**
 * Builds tenant executive indicators through business-module APIs.
 *
 * <dl>
 *   <dt>KPI
 *   <dd>An executive indicator derived from business records.
 *   <dt>Snapshot
 *   <dd>A module API projection used as reporting input.
 *   <dt>Reporting period
 *   <dd>A UTC date range used to aggregate indicators.
 *   <dt>Currency grouping
 *   <dd>The separation of monetary totals by currency.
 * </dl>
 *
 * <p>Canonical definitions are maintained in {@code docs/GLOSSARY.md}.
 */
@ApplicationModule(
        allowedDependencies = {
            "common::permission",
            "common::reporting",
            "common::tenant",
            "financial::api",
            "inventory::api",
            "logistics::api",
            "sales::api"
        })
package com.thiagsilvadev.erpapi.kpi;

import org.springframework.modulith.ApplicationModule;
