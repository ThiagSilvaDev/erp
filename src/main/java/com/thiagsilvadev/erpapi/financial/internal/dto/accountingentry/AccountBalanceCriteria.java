package com.thiagsilvadev.erpapi.financial.internal.dto.accountingentry;

import java.time.LocalDate;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AccountBalanceCriteria(
        @NotNull LocalDate from, @NotNull LocalDate to, @NotBlank @Size(min = 3, max = 3) String currencyCode) {

    @AssertTrue(message = "from must be on or before to") public boolean isOrderedRange() {
        return from == null || to == null || !from.isAfter(to);
    }
}
