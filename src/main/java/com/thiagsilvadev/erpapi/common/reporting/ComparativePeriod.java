package com.thiagsilvadev.erpapi.common.reporting;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;

public record ComparativePeriod(DateRange current, DateRange previous, List<DateRange> trendRanges) {

    public ComparativePeriod {
        Objects.requireNonNull(current, "Current range is required");
        Objects.requireNonNull(previous, "Previous range is required");
        trendRanges = List.copyOf(trendRanges);
    }

    public Instant queryFromInclusive() {
        return previous.from().atStartOfDay(ZoneOffset.UTC).toInstant();
    }

    public Instant queryToExclusive() {
        return current.to().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
    }
}
