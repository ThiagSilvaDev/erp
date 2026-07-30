@ApplicationModule(
        allowedDependencies = {
            "common::model",
            "common::exception",
            "common::numbering",
            "common::reporting",
            "common::tenant",
            "common::web",
            "company::api",
            "inventory::api",
            "sales::api",
            "user::api"
        })
package com.thiagsilvadev.erpapi.logistics;

import org.springframework.modulith.ApplicationModule;
