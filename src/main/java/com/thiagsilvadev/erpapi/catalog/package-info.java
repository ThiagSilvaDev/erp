@ApplicationModule(
        allowedDependencies = {
            "common::exception",
            "common::model",
            "common::permission",
            "common::tenant",
            "common::validation",
            "common::web",
            "company::api"
        })
package com.thiagsilvadev.erpapi.catalog;

import org.springframework.modulith.ApplicationModule;
