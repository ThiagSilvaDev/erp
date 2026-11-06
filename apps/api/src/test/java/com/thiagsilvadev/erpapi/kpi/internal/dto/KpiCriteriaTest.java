package com.thiagsilvadev.erpapi.kpi.internal.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import jakarta.validation.Validation;

import org.junit.jupiter.api.Test;

class KpiCriteriaTest {

    @Test
    void shouldRejectPartialReversedAndOversizedRanges() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();

            assertThat(validator.validate(new KpiCriteria(LocalDate.parse("2026-01-01"), null, null)))
                    .extracting(violation -> violation.getPropertyPath().toString())
                    .contains("complete");
            assertThat(validator.validate(
                            new KpiCriteria(LocalDate.parse("2026-02-01"), LocalDate.parse("2026-01-01"), null)))
                    .extracting(violation -> violation.getPropertyPath().toString())
                    .contains("ordered");
            assertThat(validator.validate(
                            new KpiCriteria(LocalDate.parse("2025-01-01"), LocalDate.parse("2026-01-02"), null)))
                    .extracting(violation -> violation.getPropertyPath().toString())
                    .contains("withinMaximumRange");
        }
    }

    @Test
    void shouldAcceptOmittedDatesAndInclusiveMaximumRange() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();

            assertThat(validator.validate(new KpiCriteria(null, null, KpiBucket.MONTH)))
                    .isEmpty();
            assertThat(validator.validate(new KpiCriteria(
                            LocalDate.parse("2025-01-01"), LocalDate.parse("2026-01-01"), KpiBucket.MONTH)))
                    .isEmpty();
        }
    }
}
