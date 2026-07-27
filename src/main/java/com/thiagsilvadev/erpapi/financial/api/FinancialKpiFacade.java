package com.thiagsilvadev.erpapi.financial.api;

import java.time.LocalDate;
import java.util.UUID;

import com.thiagsilvadev.erpapi.common.reporting.ComparativePeriod;

public interface FinancialKpiFacade {

    FinancialKpiSnapshot calculate(UUID companyId, ComparativePeriod period, LocalDate receivablesAsOf);
}
