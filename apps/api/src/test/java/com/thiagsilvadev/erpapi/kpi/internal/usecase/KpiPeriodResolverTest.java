package com.thiagsilvadev.erpapi.kpi.internal.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import com.thiagsilvadev.erpapi.kpi.internal.dto.KpiBucket;
import com.thiagsilvadev.erpapi.kpi.internal.dto.KpiCriteria;

class KpiPeriodResolverTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-07-27T15:30:00Z"), ZoneOffset.UTC);

    @Test
    void shouldDefaultToLatestThirtyUtcDaysAndDayBuckets() {
        var period = new KpiPeriodResolver(CLOCK).resolve(new KpiCriteria(null, null, null));

        assertThat(period.from()).isEqualTo("2026-06-28");
        assertThat(period.to()).isEqualTo("2026-07-27");
        assertThat(period.previousFrom()).isEqualTo("2026-05-29");
        assertThat(period.previousTo()).isEqualTo("2026-06-27");
        assertThat(period.bucket()).isEqualTo(KpiBucket.DAY);
        assertThat(period.comparativePeriod().queryFromInclusive()).isEqualTo("2026-05-29T00:00:00Z");
        assertThat(period.comparativePeriod().queryToExclusive()).isEqualTo("2026-07-28T00:00:00Z");
    }

    @Test
    void shouldCreateEqualLengthComparisonPeriod() {
        var period = new KpiPeriodResolver(CLOCK)
                .resolve(new KpiCriteria(
                        java.time.LocalDate.parse("2026-07-01"),
                        java.time.LocalDate.parse("2026-07-07"),
                        KpiBucket.WEEK));

        assertThat(period.previousFrom()).isEqualTo("2026-06-24");
        assertThat(period.previousTo()).isEqualTo("2026-06-30");
        assertThat(period.bucket()).isEqualTo(KpiBucket.WEEK);
    }

    @Test
    void shouldClipWeekAndMonthTrendRangesToRequestedPeriod() {
        var resolver = new KpiPeriodResolver(CLOCK);

        var weekly = resolver.resolve(new KpiCriteria(
                java.time.LocalDate.parse("2026-07-02"), java.time.LocalDate.parse("2026-07-15"), KpiBucket.WEEK));
        assertThat(weekly.comparativePeriod().trendRanges())
                .extracting(range -> range.from() + "/" + range.to())
                .containsExactly("2026-07-02/2026-07-05", "2026-07-06/2026-07-12", "2026-07-13/2026-07-15");

        var monthly = resolver.resolve(new KpiCriteria(
                java.time.LocalDate.parse("2026-01-15"), java.time.LocalDate.parse("2026-03-02"), KpiBucket.MONTH));
        assertThat(monthly.comparativePeriod().trendRanges())
                .extracting(range -> range.from() + "/" + range.to())
                .containsExactly("2026-01-15/2026-01-31", "2026-02-01/2026-02-28", "2026-03-01/2026-03-02");
    }
}
