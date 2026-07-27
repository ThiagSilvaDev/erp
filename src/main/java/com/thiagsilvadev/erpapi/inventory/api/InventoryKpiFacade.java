package com.thiagsilvadev.erpapi.inventory.api;

import java.time.Instant;
import java.util.UUID;

public interface InventoryKpiFacade {

    InventoryKpiSnapshot load(UUID companyId, Instant fromInclusive, Instant toExclusive);
}
