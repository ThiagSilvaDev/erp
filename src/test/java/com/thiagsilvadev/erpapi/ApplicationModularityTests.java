package com.thiagsilvadev.erpapi;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ApplicationModularityTests {

    @Test
    void verifiesModularStructure() {
        ApplicationModules.of(ErpApiApplication.class).verify();
    }
}
