@ApplicationModule(
        allowedDependencies = {
            "common::exception",
            "common::model",
            "common::permission",
            "common::tenant",
            "common::transaction",
            "common::validation",
            "common::web",
            "company::api"
        })
package com.thiagsilvadev.erpapi.user;

import org.springframework.modulith.ApplicationModule;
