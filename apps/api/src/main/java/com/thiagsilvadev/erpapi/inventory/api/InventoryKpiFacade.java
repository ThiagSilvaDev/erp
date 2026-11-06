package com.thiagsilvadev.erpapi.inventory.api;

import java.util.UUID;

import com.thiagsilvadev.erpapi.common.reporting.ComparativePeriod;

public interface InventoryKpiFacade {

    InventoryKpiSnapshot calculate(UUID companyId, ComparativePeriod period);
}
