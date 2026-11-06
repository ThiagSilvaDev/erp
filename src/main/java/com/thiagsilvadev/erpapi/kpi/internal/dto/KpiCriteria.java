package com.thiagsilvadev.erpapi.kpi.internal.dto;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import jakarta.validation.constraints.AssertTrue;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;

public record KpiCriteria(
        @Schema(
                        description =
                                "Inclusive UTC date; from and to must both be provided or both omitted (defaults to last 30 UTC days, max 366 days)",
                        example = "2026-07-01")
                LocalDate from,
        @Schema(
                        description =
                                "Inclusive UTC date; from and to must both be provided or both omitted (defaults to last 30 UTC days, max 366 days)",
                        example = "2026-07-01")
                LocalDate to,
        @Schema(
                        description = "Trend aggregation bucket (DAY, WEEK, MONTH); defaults to DAY when omitted",
                        example = "DAY")
                KpiBucket bucket) {

    @Parameter(hidden = true)
    @AssertTrue(message = "from and to must either both be provided or both be omitted") public boolean isComplete() {
        return (from == null) == (to == null);
    }

    @Parameter(hidden = true)
    @AssertTrue(message = "from must be on or before to") public boolean isOrdered() {
        return from == null || to == null || !from.isAfter(to);
    }

    @Parameter(hidden = true)
    @AssertTrue(message = "the requested period must not exceed 366 days") public boolean isWithinMaximumRange() {
        return from == null || to == null || from.isAfter(to) || ChronoUnit.DAYS.between(from, to) < 366;
    }
}
