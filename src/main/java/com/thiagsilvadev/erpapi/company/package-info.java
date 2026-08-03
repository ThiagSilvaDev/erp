@ApplicationModule(
        allowedDependencies = {
            "common::model",
            "common::address",
            "common::exception",
            "common::numbering",
            "common::validation",
            "common::web"
        })
package com.thiagsilvadev.erpapi.company;

import org.springframework.modulith.ApplicationModule;
