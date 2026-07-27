package com.thiagsilvadev.erpapi.kpi.internal.usecase;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Component;

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

        Instant queryFrom() {
            return previousFrom.atStartOfDay(ZoneOffset.UTC).toInstant();
        }

        Instant queryToExclusive() {
            return to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        }
    }
}
