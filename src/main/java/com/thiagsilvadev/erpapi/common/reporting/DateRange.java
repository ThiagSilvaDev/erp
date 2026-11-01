package com.thiagsilvadev.erpapi.common.reporting;

import java.time.LocalDate;

import com.thiagsilvadev.erpapi.common.domain.validation.DomainAssert;

public record DateRange(LocalDate from, LocalDate to) {

    public DateRange {
        DomainAssert.requireNotNull(from, "Range start");
        DomainAssert.requireNotNull(to, "Range end");
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("Range start must be on or before range end");
        }
    }

    public boolean contains(LocalDate date) {
        return !date.isBefore(from) && !date.isAfter(to);
    }
}
