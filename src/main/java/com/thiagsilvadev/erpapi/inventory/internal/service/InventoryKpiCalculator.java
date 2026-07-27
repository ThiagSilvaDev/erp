package com.thiagsilvadev.erpapi.inventory.internal.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;

import com.thiagsilvadev.erpapi.common.reporting.ComparativePeriod;
import com.thiagsilvadev.erpapi.common.reporting.DateRange;
import com.thiagsilvadev.erpapi.inventory.api.InventoryKpiSnapshot;

@Component
class InventoryKpiCalculator {

    InventoryKpiSnapshot calculate(
            InventoryKpiSnapshot.Current current, List<MovementRow> rows, ComparativePeriod period) {
        InventoryKpiSnapshot.Movements currentMovements = totals(rows, period.current());
        InventoryKpiSnapshot.Movements previousMovements = totals(rows, period.previous());
        var trend =
                period.trendRanges().stream().map(range -> trend(rows, range)).toList();
        return new InventoryKpiSnapshot(current, currentMovements, previousMovements, trend);
    }

    private static InventoryKpiSnapshot.Trend trend(List<MovementRow> rows, DateRange range) {
        var totals = totals(rows, range);
        return new InventoryKpiSnapshot.Trend(
                range.from(),
                range.to(),
                totals.inbound(),
                totals.outbound(),
                totals.reserved(),
                totals.released(),
                totals.adjusted());
    }

    private static InventoryKpiSnapshot.Movements totals(List<MovementRow> rows, DateRange range) {
        long inbound = 0;
        long outbound = 0;
        long reserved = 0;
        long released = 0;
        long adjusted = 0;
        for (MovementRow row : rows) {
            if (range.contains(row.date())) {
                inbound += row.inbound();
                outbound += row.outbound();
                reserved += row.reserved();
                released += row.released();
                adjusted += row.adjusted();
            }
        }
        return new InventoryKpiSnapshot.Movements(inbound, outbound, reserved, released, adjusted);
    }

    record MovementRow(LocalDate date, long inbound, long outbound, long reserved, long released, long adjusted) {}
}
