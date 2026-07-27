package com.thiagsilvadev.erpapi.sales.api;

import java.util.UUID;

import com.thiagsilvadev.erpapi.common.reporting.ComparativePeriod;

public interface SalesKpiFacade {

    SalesKpiSnapshot calculate(UUID companyId, ComparativePeriod period);
}
