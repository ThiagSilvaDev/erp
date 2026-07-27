package com.thiagsilvadev.erpapi.financial.api;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public interface FinancialKpiFacade {

    FinancialKpiSnapshot load(UUID companyId, Instant fromInclusive, Instant toExclusive, LocalDate receivablesAsOf);
}
