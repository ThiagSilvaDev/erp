@ApplicationModule(
        allowedDependencies = {
            "common::model",
            "common::exception",
            "common::numbering",
            "common::web",
            "company::api",
            "customer::api",
            "sales::api"
        })
package com.thiagsilvadev.erpapi.financial;

import org.springframework.modulith.ApplicationModule;
