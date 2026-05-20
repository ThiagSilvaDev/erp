@ApplicationModule(
        allowedDependencies = {
            "common::model",
            "common::exception",
            "common::numbering",
            "common::web",
            "inventory::api",
            "sales::api"
        })
package com.thiagsilvadev.erpapi.logistics;

import org.springframework.modulith.ApplicationModule;
