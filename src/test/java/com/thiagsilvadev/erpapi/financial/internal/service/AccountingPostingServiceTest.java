package com.thiagsilvadev.erpapi.financial.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.thiagsilvadev.erpapi.financial.internal.model.*;
import com.thiagsilvadev.erpapi.financial.internal.repository.AccountingEntryRepository;

@ExtendWith(MockitoExtension.class)
class AccountingPostingServiceTest {

    private static final UUID COMPANY_ID = UUID.randomUUID();
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-08-03T12:00:00Z"), ZoneOffset.UTC);

    @Mock
    private AccountingEntryRepository entryRepository;

    @Mock
    private ChartOfAccountsService chartOfAccountsService;

    @Test
    void shouldPostInvoiceRevenueAsCredit() {
        AccountingAccount revenue = AccountingAccount.create(
                COMPANY_ID,
                "3.1",
                "Receita",
                null,
                null,
                AccountingAccountType.REVENUE,
                AccountingSystemRole.REVENUE_SALES,
                true);
        ReflectionTestUtils.setField(revenue, "id", UUID.randomUUID());
        Invoice invoice = Invoice.create(
                10L,
                COMPANY_ID,
                BillingPartyType.CUSTOMER,
                UUID.randomUUID(),
                null,
                new BigDecimal("250.00"),
                "BRL",
                LocalDate.now());
        ReflectionTestUtils.setField(invoice, "id", UUID.randomUUID());
        when(chartOfAccountsService.requireBySystemRole(COMPANY_ID, AccountingSystemRole.REVENUE_SALES))
                .thenReturn(revenue);
        when(entryRepository.findByCompanyIdAndSourceTypeAndSourceIdAndStatus(
                        COMPANY_ID, AccountingEntrySourceType.INVOICE, invoice.getId(), AccountingEntryStatus.POSTED))
                .thenReturn(List.of());
        when(entryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service().postInvoiceRevenue(invoice);

        ArgumentCaptor<AccountingEntry> captor = ArgumentCaptor.forClass(AccountingEntry.class);
        verify(chartOfAccountsService).ensureDefaultAccounts(COMPANY_ID);
        verify(entryRepository).save(captor.capture());
        assertThat(captor.getValue().getAccountId()).isEqualTo(revenue.getId());
        assertThat(captor.getValue().getDirection()).isEqualTo(AccountingEntryDirection.CREDIT);
        assertThat(captor.getValue().getAmount()).isEqualByComparingTo("250.00");
        assertThat(captor.getValue().getSourceType()).isEqualTo(AccountingEntrySourceType.INVOICE);
        assertThat(captor.getValue().getCompetenceDate()).isEqualTo(LocalDate.of(2026, 8, 3));
    }

    @Test
    void shouldReversePostedInvoiceRevenue() {
        UUID invoiceId = UUID.randomUUID();
        AccountingEntry posted = AccountingEntry.create(
                COMPANY_ID,
                UUID.randomUUID(),
                LocalDate.of(2026, 7, 1),
                new BigDecimal("100.00"),
                "BRL",
                AccountingEntryDirection.CREDIT,
                AccountingEntrySourceType.INVOICE,
                invoiceId,
                "Invoice 1");
        Invoice invoice = Invoice.create(
                1L,
                COMPANY_ID,
                BillingPartyType.CUSTOMER,
                UUID.randomUUID(),
                null,
                new BigDecimal("100.00"),
                "BRL",
                LocalDate.now());
        ReflectionTestUtils.setField(invoice, "id", invoiceId);
        when(entryRepository.findByCompanyIdAndSourceTypeAndSourceIdAndStatus(
                        COMPANY_ID, AccountingEntrySourceType.INVOICE, invoiceId, AccountingEntryStatus.POSTED))
                .thenReturn(List.of(posted));
        when(entryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service().reverseInvoiceRevenue(invoice);

        ArgumentCaptor<AccountingEntry> captor = ArgumentCaptor.forClass(AccountingEntry.class);
        verify(entryRepository, times(2)).save(captor.capture());
        assertThat(posted.getStatus()).isEqualTo(AccountingEntryStatus.REVERSED);
        assertThat(captor.getAllValues().get(1).getDirection()).isEqualTo(AccountingEntryDirection.DEBIT);
        assertThat(captor.getAllValues().get(1).getSourceType()).isEqualTo(AccountingEntrySourceType.ADJUSTMENT);
    }

    private AccountingPostingService service() {
        return new AccountingPostingService(entryRepository, chartOfAccountsService, CLOCK);
    }
}
