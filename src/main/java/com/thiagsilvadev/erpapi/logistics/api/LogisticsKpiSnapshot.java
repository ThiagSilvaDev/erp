package com.thiagsilvadev.erpapi.logistics.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record LogisticsKpiSnapshot(Current current, List<Daily> daily) {

    public LogisticsKpiSnapshot {
        daily = List.copyOf(daily);
    }

    public record Current(long created, long dispatchAssigned, long loaded, long inTransit) {}

    public record Daily(
            LocalDate date,
            long created,
            long delivered,
            long cancelled,
            BigDecimal deliveryHoursTotal,
            long deliveriesWithDuration) {}
}
