package com.thiagsilvadev.erpapi.logistics.internal.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.logistics.api.LogisticsKpiFacade;
import com.thiagsilvadev.erpapi.logistics.api.LogisticsKpiSnapshot;
import com.thiagsilvadev.erpapi.logistics.internal.repository.ShipmentRepository;

@Service
class LogisticsKpiService implements LogisticsKpiFacade {

    private final ShipmentRepository shipmentRepository;

    LogisticsKpiService(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public LogisticsKpiSnapshot load(UUID companyId, Instant fromInclusive, Instant toExclusive) {
        var currentRow = shipmentRepository.findCurrentKpiRow(companyId);
        var current = new LogisticsKpiSnapshot.Current(
                currentRow.getCreated(),
                currentRow.getDispatchAssigned(),
                currentRow.getLoaded(),
                currentRow.getInTransit());
        var daily = shipmentRepository.findDailyKpiRows(companyId, fromInclusive, toExclusive).stream()
                .map(row -> new LogisticsKpiSnapshot.Daily(
                        row.getDate(),
                        row.getCreated(),
                        row.getDelivered(),
                        row.getCancelled(),
                        row.getDeliveryHoursTotal(),
                        row.getDeliveriesWithDuration()))
                .toList();
        return new LogisticsKpiSnapshot(current, daily);
    }
}
