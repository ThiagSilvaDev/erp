@ApplicationModule(
        allowedDependencies = {
            "common::model",
            "common::exception",
            "common::numbering",
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
