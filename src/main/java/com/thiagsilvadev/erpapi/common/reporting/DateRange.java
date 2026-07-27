package com.thiagsilvadev.erpapi.common.reporting;

import java.time.LocalDate;
import java.util.Objects;

public record DateRange(LocalDate from, LocalDate to) {

    public DateRange {
        Objects.requireNonNull(from, "Range start is required");
        Objects.requireNonNull(to, "Range end is required");
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("Range start must be on or before range end");
        }
    }

    public boolean contains(LocalDate date) {
        return !date.isBefore(from) && !date.isAfter(to);
    }
}
