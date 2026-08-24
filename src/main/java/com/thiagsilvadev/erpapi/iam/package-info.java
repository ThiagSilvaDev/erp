@ApplicationModule(
        allowedDependencies = {
            "common::exception",
            "common::model",
            "common::tenant",
            "common::validation",
            "common::web",
            "company::api",
            "iam.accesscontrol::api",
            "user::api"
        })
package com.thiagsilvadev.erpapi.iam;

import org.springframework.modulith.ApplicationModule;
