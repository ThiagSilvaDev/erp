package com.thiagsilvadev.erpapi.inventory.internal.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.thiagsilvadev.erpapi.common.reporting.ComparativePeriod;
import com.thiagsilvadev.erpapi.common.reporting.DateRange;
import com.thiagsilvadev.erpapi.inventory.api.InventoryKpiSnapshot;

class InventoryKpiCalculatorTest {

    @Test
    void shouldCalculateAllMovementCategoriesAndGapFilledTrends() {
        var rows = List.of(
                new InventoryKpiCalculator.MovementRow(date("2026-06-30"), 2, 1, 0, 0, 0),
                new InventoryKpiCalculator.MovementRow(date("2026-07-01"), 10, 3, 2, 1, 4));
        var current = new InventoryKpiSnapshot.Current(4, 100, 20, 80, 1);

        var result = new InventoryKpiCalculator().calculate(current, rows, period());

        assertThat(result.current()).isEqualTo(current);
        assertThat(result.currentMovements()).isEqualTo(new InventoryKpiSnapshot.Movements(10, 3, 2, 1, 4));
        assertThat(result.previousMovements()).isEqualTo(new InventoryKpiSnapshot.Movements(2, 1, 0, 0, 0));
        assertThat(result.trend()).hasSize(2);
        assertThat(result.trend().get(1).inbound()).isZero();
    }

    private static ComparativePeriod period() {
        return new ComparativePeriod(
                range("2026-07-01", "2026-07-02"),
                range("2026-06-29", "2026-06-30"),
                List.of(range("2026-07-01", "2026-07-01"), range("2026-07-02", "2026-07-02")));
    }

    private static DateRange range(String from, String to) {
        return new DateRange(date(from), date(to));
    }

    private static LocalDate date(String value) {
        return LocalDate.parse(value);
    }
}
