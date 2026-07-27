@ApplicationModule(
        allowedDependencies = {
            "common::exception",
            "common::model",
            "common::tenant",
            "common::web",
            "catalog::api",
            "company::api",
            "customer::api",
            "financial::api",
            "inventory::api",
            "kpi::api",
            "logistics::api",
            "sales::api",
            "user::api"
        })
package com.thiagsilvadev.erpapi.iam;

import org.springframework.modulith.ApplicationModule;
