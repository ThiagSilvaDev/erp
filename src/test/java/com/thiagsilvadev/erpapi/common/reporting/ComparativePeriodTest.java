package com.thiagsilvadev.erpapi.common.reporting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class ComparativePeriodTest {

    @Test
    void shouldExposeUtcQueryBoundariesAndDefensivelyCopyTrends() {
        List<DateRange> trends = new ArrayList<>(List.of(range("2026-07-01", "2026-07-02")));
        var period =
                new ComparativePeriod(range("2026-07-01", "2026-07-02"), range("2026-06-29", "2026-06-30"), trends);

        trends.clear();

        assertThat(period.queryFromInclusive()).isEqualTo("2026-06-29T00:00:00Z");
        assertThat(period.queryToExclusive()).isEqualTo("2026-07-03T00:00:00Z");
        assertThat(period.trendRanges()).hasSize(1);
    }

    @Test
    void shouldRejectAnInvertedDateRange() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> range("2026-07-02", "2026-07-01"))
                .withMessage("Range start must be on or before range end");
    }

    private static DateRange range(String from, String to) {
        return new DateRange(LocalDate.parse(from), LocalDate.parse(to));
    }
}
