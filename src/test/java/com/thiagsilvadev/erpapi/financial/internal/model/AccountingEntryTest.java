package com.thiagsilvadev.erpapi.financial.internal.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class AccountingEntryTest {
    private static final UUID COMPANY_ID = UUID.randomUUID();
    private static final UUID ACCOUNT_ID = UUID.randomUUID();

    @Test
    void shouldCreatePostedEntry() {
        AccountingEntry entry = AccountingEntry.create(
                COMPANY_ID,
                ACCOUNT_ID,
                LocalDate.of(2026, 1, 15),
                new BigDecimal("100.00"),
                "brl",
                AccountingEntryDirection.CREDIT,
                AccountingEntrySourceType.INVOICE,
                UUID.randomUUID(),
                " Invoice revenue ");

        assertThat(entry.getCurrencyCode()).isEqualTo("BRL");
        assertThat(entry.getDirection()).isEqualTo(AccountingEntryDirection.CREDIT);
        assertThat(entry.getStatus()).isEqualTo(AccountingEntryStatus.POSTED);
        assertThat(entry.getDescription()).isEqualTo("Invoice revenue");
    }

    @Test
    void shouldReversePostedEntry() {
        UUID sourceId = UUID.randomUUID();
        AccountingEntry entry = AccountingEntry.create(
                COMPANY_ID,
                ACCOUNT_ID,
                LocalDate.of(2026, 1, 15),
                new BigDecimal("100.00"),
                "BRL",
                AccountingEntryDirection.CREDIT,
                AccountingEntrySourceType.INVOICE,
                sourceId,
                "Invoice");

        AccountingEntry reversal = entry.reverse(LocalDate.of(2026, 1, 20), "Reversal");

        assertThat(entry.getStatus()).isEqualTo(AccountingEntryStatus.REVERSED);
        assertThat(reversal.getStatus()).isEqualTo(AccountingEntryStatus.POSTED);
        assertThat(reversal.getDirection()).isEqualTo(AccountingEntryDirection.DEBIT);
        assertThat(reversal.getSourceType()).isEqualTo(AccountingEntrySourceType.ADJUSTMENT);
        assertThat(reversal.getSourceId()).isEqualTo(sourceId);
        assertThat(reversal.getCompetenceDate()).isEqualTo(LocalDate.of(2026, 1, 20));
        assertThat(reversal.getAmount()).isEqualByComparingTo("100.00");
    }

    @Test
    void shouldFailWhenAmountIsNotPositive() {
        assertThatThrownBy(() -> AccountingEntry.create(
                        COMPANY_ID,
                        ACCOUNT_ID,
                        LocalDate.now(),
                        BigDecimal.ZERO,
                        "BRL",
                        AccountingEntryDirection.DEBIT,
                        AccountingEntrySourceType.MANUAL,
                        null,
                        null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Amount must be positive");
    }

    @Test
    void shouldFailWhenReversingAlreadyReversedEntry() {
        AccountingEntry entry = AccountingEntry.create(
                COMPANY_ID,
                ACCOUNT_ID,
                LocalDate.now(),
                new BigDecimal("10.00"),
                "BRL",
                AccountingEntryDirection.DEBIT,
                AccountingEntrySourceType.MANUAL,
                null,
                null);
        entry.reverse(LocalDate.now(), "once");

        assertThatThrownBy(() -> entry.reverse(LocalDate.now(), "twice"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Only posted entries can be reversed");
    }
}
