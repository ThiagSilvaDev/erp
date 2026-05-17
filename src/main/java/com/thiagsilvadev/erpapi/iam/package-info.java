@ApplicationModule(
        allowedDependencies = {
            "common::exception",
            "common::model",
            "common::web",
            "catalog::api",
            "company::api",
            "customer::api",
            "financial::api",
            "inventory::api",
            "sales::api",
            "user::api"
        })
package com.thiagsilvadev.erpapi.iam;

import org.springframework.modulith.ApplicationModule;
