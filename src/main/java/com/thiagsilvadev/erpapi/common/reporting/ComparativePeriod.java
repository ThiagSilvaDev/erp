package com.thiagsilvadev.erpapi.common.reporting;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import com.thiagsilvadev.erpapi.common.domain.validation.DomainAssert;

public record ComparativePeriod(DateRange current, DateRange previous, List<DateRange> trendRanges) {

    public ComparativePeriod {
        DomainAssert.requireNotNull(current, "Current range");
        DomainAssert.requireNotNull(previous, "Previous range");
        trendRanges = List.copyOf(trendRanges);
    }

    public Instant queryFromInclusive() {
        return previous.from().atStartOfDay(ZoneOffset.UTC).toInstant();
    }

    public Instant queryToExclusive() {
        return current.to().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
    }
}
