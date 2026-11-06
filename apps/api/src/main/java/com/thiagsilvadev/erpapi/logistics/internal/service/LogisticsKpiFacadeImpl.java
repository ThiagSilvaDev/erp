package com.thiagsilvadev.erpapi.logistics.internal.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.reporting.ComparativePeriod;
import com.thiagsilvadev.erpapi.logistics.api.LogisticsKpiFacade;
import com.thiagsilvadev.erpapi.logistics.api.LogisticsKpiSnapshot;
import com.thiagsilvadev.erpapi.logistics.internal.repository.ShipmentRepository;

@Service
class LogisticsKpiFacadeImpl implements LogisticsKpiFacade {

    private final ShipmentRepository shipmentRepository;
    private final LogisticsKpiCalculator calculator;

    LogisticsKpiFacadeImpl(ShipmentRepository shipmentRepository, LogisticsKpiCalculator calculator) {
        this.shipmentRepository = shipmentRepository;
        this.calculator = calculator;
    }

    @Override
    @Transactional(readOnly = true)
    public LogisticsKpiSnapshot calculate(UUID companyId, ComparativePeriod period) {
        var currentRow = shipmentRepository.findCurrentKpiRow(companyId);
        var current = new LogisticsKpiSnapshot.Current(
                currentRow.getCreated(),
                currentRow.getDispatchAssigned(),
                currentRow.getLoaded(),
                currentRow.getInTransit());
        var daily =
                shipmentRepository
                        .findDailyKpiRows(companyId, period.queryFromInclusive(), period.queryToExclusive())
                        .stream()
                        .map(row -> new LogisticsKpiCalculator.DailyRow(
                                row.getDate(),
                                row.getCreated(),
                                row.getDelivered(),
                                row.getCancelled(),
                                row.getDeliveryHoursTotal(),
                                row.getDeliveriesWithDuration()))
                        .toList();
        return calculator.calculate(current, daily, period);
    }
}
