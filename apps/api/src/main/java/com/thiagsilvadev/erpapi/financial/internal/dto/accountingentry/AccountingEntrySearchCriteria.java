package com.thiagsilvadev.erpapi.financial.internal.dto.accountingentry;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.AssertTrue;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;

public record AccountingEntrySearchCriteria(
        UUID accountId,
        @Schema(
                        description =
                                "Inclusive date boundary (from <= to; on entry search, both from and to must be provided together or both omitted)",
                        example = "2026-07-01")
                LocalDate from,
        @Schema(
                        description =
                                "Inclusive date boundary (from <= to; on entry search, both from and to must be provided together or both omitted)",
                        example = "2026-07-31")
                LocalDate to) {

    @Parameter(hidden = true)
    @AssertTrue(message = "from and to must either both be provided or both be omitted") public boolean isCompleteRange() {
        return (from == null) == (to == null);
    }

    @Parameter(hidden = true)
    @AssertTrue(message = "from must be on or before to") public boolean isOrderedRange() {
        return from == null || to == null || !from.isAfter(to);
    }
}
