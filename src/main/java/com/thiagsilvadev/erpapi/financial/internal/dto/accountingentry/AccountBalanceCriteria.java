package com.thiagsilvadev.erpapi.financial.internal.dto.accountingentry;

import java.time.LocalDate;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;

public record AccountBalanceCriteria(
        @NotNull @Schema(
                        description =
                                "Inclusive date boundary (from <= to; on entry search, both from and to must be provided together or both omitted)",
                        example = "2026-07-01")
                LocalDate from,
        @NotNull @Schema(
                        description =
                                "Inclusive date boundary (from <= to; on entry search, both from and to must be provided together or both omitted)",
                        example = "2026-07-31")
                LocalDate to,
        @NotBlank @Size(min = 3, max = 3) @Schema(description = "Three-letter ISO 4217 currency code, normalized to uppercase", example = "BRL")
                String currencyCode) {

    @Parameter(hidden = true)
    @AssertTrue(message = "from must be on or before to") public boolean isOrderedRange() {
        return from == null || to == null || !from.isAfter(to);
    }
}
