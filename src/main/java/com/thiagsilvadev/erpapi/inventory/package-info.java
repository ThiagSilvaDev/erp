@ApplicationModule(
        allowedDependencies = {
            "common::exception",
            "common::model",
            "common::validation",
            "common::numbering",
            "common::web",
            "catalog::api"
        })
package com.thiagsilvadev.erpapi.inventory;

import org.springframework.modulith.ApplicationModule;
