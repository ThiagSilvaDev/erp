package com.thiagsilvadev.erpapi.financial.internal.dto.accountingentry;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import jakarta.validation.Validation;

import org.junit.jupiter.api.Test;

class AccountBalanceCriteriaTest {

    @Test
    void shouldRejectReversedDateRange() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            var criteria =
                    new AccountBalanceCriteria(LocalDate.parse("2026-02-01"), LocalDate.parse("2026-01-01"), "BRL");

            assertThat(validator.validate(criteria))
                    .extracting(violation -> violation.getPropertyPath().toString())
                    .contains("orderedRange");
        }
    }

    @Test
    void shouldAcceptOrderedDateRange() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            var criteria =
                    new AccountBalanceCriteria(LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-31"), "BRL");

            assertThat(validator.validate(criteria)).isEmpty();
        }
    }
}
