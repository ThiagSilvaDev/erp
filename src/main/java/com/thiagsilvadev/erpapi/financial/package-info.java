@ApplicationModule(
        allowedDependencies = {"common::model", "common::exception", "common::web", "customer::api", "sales::api"})
package com.thiagsilvadev.erpapi.financial;

import org.springframework.modulith.ApplicationModule;
