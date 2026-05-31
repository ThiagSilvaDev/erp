@ApplicationModule(
        allowedDependencies = {
            "common::model",
            "common::address",
            "common::exception",
            "common::tenant",
            "common::web",
            "company::api"
        })
package com.thiagsilvadev.erpapi.customer;

import org.springframework.modulith.ApplicationModule;
