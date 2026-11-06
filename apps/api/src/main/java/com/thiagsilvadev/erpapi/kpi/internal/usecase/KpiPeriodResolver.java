package com.thiagsilvadev.erpapi.kpi.internal.usecase;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.thiagsilvadev.erpapi.common.reporting.ComparativePeriod;
import com.thiagsilvadev.erpapi.common.reporting.DateRange;
import com.thiagsilvadev.erpapi.kpi.internal.dto.KpiBucket;
import com.thiagsilvadev.erpapi.kpi.internal.dto.KpiCriteria;

@Component
public class KpiPeriodResolver {

    private final Clock clock;

    public KpiPeriodResolver(Clock clock) {
        this.clock = clock;
    }

    KpiPeriod resolve(KpiCriteria criteria) {
        Instant generatedAt = clock.instant();
        LocalDate today = generatedAt.atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate to = criteria.to() == null ? today : criteria.to();
        LocalDate from = criteria.from() == null ? to.minusDays(29) : criteria.from();
        long days = ChronoUnit.DAYS.between(from, to) + 1;
        LocalDate previousTo = from.minusDays(1);
        LocalDate previousFrom = from.minusDays(days);
        return new KpiPeriod(
                generatedAt,
                from,
                to,
                previousFrom,
                previousTo,
                criteria.bucket() == null ? KpiBucket.DAY : criteria.bucket());
    }

    record KpiPeriod(
            Instant generatedAt,
            LocalDate from,
            LocalDate to,
            LocalDate previousFrom,
            LocalDate previousTo,
            KpiBucket bucket) {

        ComparativePeriod comparativePeriod() {
            return new ComparativePeriod(
                    new DateRange(from, to), new DateRange(previousFrom, previousTo), trendRanges());
        }

        private List<DateRange> trendRanges() {
            List<DateRange> ranges = new ArrayList<>();
            LocalDate cursor = from;
            while (!cursor.isAfter(to)) {
                LocalDate end = bucket.end(cursor);
                if (end.isAfter(to)) {
                    end = to;
                }
                ranges.add(new DateRange(cursor, end));
                cursor = end.plusDays(1);
            }
            return ranges;
        }
    }
}
