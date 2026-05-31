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
            "logistics::api",
            "sales::api",
            "user::api"
        })
package com.thiagsilvadev.erpapi.iam;

import org.springframework.modulith.ApplicationModule;
