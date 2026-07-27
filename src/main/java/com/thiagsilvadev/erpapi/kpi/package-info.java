@ApplicationModule(
        allowedDependencies = {"common::tenant", "financial::api", "inventory::api", "logistics::api", "sales::api"})
package com.thiagsilvadev.erpapi.kpi;

import org.springframework.modulith.ApplicationModule;
