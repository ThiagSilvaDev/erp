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
        assertThat(period.queryFrom()).isEqualTo("2026-05-29T00:00:00Z");
        assertThat(period.queryToExclusive()).isEqualTo("2026-07-28T00:00:00Z");
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
}
