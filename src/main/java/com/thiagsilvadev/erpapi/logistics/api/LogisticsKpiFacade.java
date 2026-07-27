package com.thiagsilvadev.erpapi.logistics.api;

import java.time.Instant;
import java.util.UUID;

public interface LogisticsKpiFacade {

    LogisticsKpiSnapshot load(UUID companyId, Instant fromInclusive, Instant toExclusive);
}
