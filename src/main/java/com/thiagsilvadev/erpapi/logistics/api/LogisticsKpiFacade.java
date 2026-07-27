package com.thiagsilvadev.erpapi.logistics.api;

import java.util.UUID;

import com.thiagsilvadev.erpapi.common.reporting.ComparativePeriod;

public interface LogisticsKpiFacade {

    LogisticsKpiSnapshot calculate(UUID companyId, ComparativePeriod period);
}
