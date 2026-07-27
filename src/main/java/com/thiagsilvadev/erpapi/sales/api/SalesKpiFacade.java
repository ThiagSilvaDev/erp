package com.thiagsilvadev.erpapi.sales.api;

import java.time.Instant;
import java.util.UUID;

public interface SalesKpiFacade {

    SalesKpiSnapshot load(UUID companyId, Instant fromInclusive, Instant toExclusive);
}
