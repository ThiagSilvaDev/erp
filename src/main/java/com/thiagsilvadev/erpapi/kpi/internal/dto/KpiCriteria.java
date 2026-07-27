package com.thiagsilvadev.erpapi.kpi.internal.dto;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import jakarta.validation.constraints.AssertTrue;

public record KpiCriteria(LocalDate from, LocalDate to, KpiBucket bucket) {

    @AssertTrue(message = "from and to must either both be provided or both be omitted") public boolean isComplete() {
        return (from == null) == (to == null);
    }

    @AssertTrue(message = "from must be on or before to") public boolean isOrdered() {
        return from == null || to == null || !from.isAfter(to);
    }

    @AssertTrue(message = "the requested period must not exceed 366 days") public boolean isWithinMaximumRange() {
        return from == null || to == null || from.isAfter(to) || ChronoUnit.DAYS.between(from, to) < 366;
    }
}
