@ApplicationModule(
        allowedDependencies = {
            "common::exception",
            "common::model",
            "common::validation",
            "common::numbering",
            "common::tenant",
            "common::web",
            "catalog::api",
            "company::api"
        })
package com.thiagsilvadev.erpapi.inventory;

import org.springframework.modulith.ApplicationModule;
