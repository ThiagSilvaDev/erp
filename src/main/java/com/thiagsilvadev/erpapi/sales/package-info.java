@ApplicationModule(
        allowedDependencies = {
            "common::model",
            "common::exception",
            "common::web",
            "catalog::api",
            "customer::api",
            "inventory::api"
        })
package com.thiagsilvadev.erpapi.sales;

import org.springframework.modulith.ApplicationModule;
