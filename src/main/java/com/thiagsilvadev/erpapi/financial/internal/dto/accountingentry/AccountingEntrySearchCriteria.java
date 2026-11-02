package com.thiagsilvadev.erpapi.financial.internal.dto.accountingentry;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.AssertTrue;

public record AccountingEntrySearchCriteria(UUID accountId, LocalDate from, LocalDate to) {

    @AssertTrue(message = "from and to must either both be provided or both be omitted") public boolean isCompleteRange() {
        return (from == null) == (to == null);
    }

    @AssertTrue(message = "from must be on or before to") public boolean isOrderedRange() {
        return from == null || to == null || !from.isAfter(to);
    }
}
