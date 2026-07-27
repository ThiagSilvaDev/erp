package com.thiagsilvadev.erpapi.kpi.internal.dto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

public enum KpiBucket {
    DAY,
    WEEK,
    MONTH;

    public LocalDate start(LocalDate date) {
        return switch (this) {
            case DAY -> date;
            case WEEK -> date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case MONTH -> date.withDayOfMonth(1);
        };
    }

    public LocalDate end(LocalDate date) {
        return switch (this) {
            case DAY -> date;
            case WEEK -> date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            case MONTH -> date.with(TemporalAdjusters.lastDayOfMonth());
        };
    }
}
