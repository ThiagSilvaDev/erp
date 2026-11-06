package com.thiagsilvadev.erpapi.financial.internal.dto.accountingentry;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import jakarta.validation.Validation;

import org.junit.jupiter.api.Test;

class AccountingEntrySearchCriteriaTest {

    @Test
    void shouldRejectIncompleteAndReversedRanges() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();

            assertThat(validator.validate(new AccountingEntrySearchCriteria(null, LocalDate.parse("2026-01-01"), null)))
                    .extracting(violation -> violation.getPropertyPath().toString())
                    .contains("completeRange");
            assertThat(validator.validate(new AccountingEntrySearchCriteria(
                            null, LocalDate.parse("2026-02-01"), LocalDate.parse("2026-01-01"))))
                    .extracting(violation -> violation.getPropertyPath().toString())
                    .contains("orderedRange");
        }
    }

    @Test
    void shouldAcceptOmittedAndOrderedRanges() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();

            assertThat(validator.validate(new AccountingEntrySearchCriteria(null, null, null)))
                    .isEmpty();
            assertThat(validator.validate(new AccountingEntrySearchCriteria(
                            null, LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-31"))))
                    .isEmpty();
        }
    }
}
