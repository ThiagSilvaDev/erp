package com.thiagsilvadev.erpapi.financial.internal.seed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.util.ReflectionTestUtils;

import com.thiagsilvadev.erpapi.common.domain.numbering.BusinessNumberService;
import com.thiagsilvadev.erpapi.company.api.CompanyFacade;
import com.thiagsilvadev.erpapi.company.api.CompanySnapshot;
import com.thiagsilvadev.erpapi.customer.api.CustomerFacade;
import com.thiagsilvadev.erpapi.customer.api.CustomerSnapshot;
import com.thiagsilvadev.erpapi.financial.internal.model.*;
import com.thiagsilvadev.erpapi.financial.internal.repository.*;
import com.thiagsilvadev.erpapi.financial.internal.service.AccountingPostingService;
import com.thiagsilvadev.erpapi.financial.internal.service.ChartOfAccountsService;

class FinancialDevSeederTest {
    private static final UUID COMPANY_ID = UUID.randomUUID();
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-07-27T12:00:00Z"), ZoneOffset.UTC);

    @Test
    void shouldSeedInvoicesPaymentsAndCollectionsAcrossDates() {
        InvoiceRepository invoiceRepository = mock(InvoiceRepository.class);
        LedgerEntryRepository ledgerRepository = mock(LedgerEntryRepository.class);
        FinancialAccountRepository accountRepository = mock(FinancialAccountRepository.class);
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        FinancialEntryRepository entryRepository = mock(FinancialEntryRepository.class);
        AccountingEntryRepository accountingEntryRepository = mock(AccountingEntryRepository.class);
        CompanyFacade companyFacade = mock(CompanyFacade.class);
        CustomerFacade customerFacade = mock(CustomerFacade.class);
        BusinessNumberService businessNumberService = mock(BusinessNumberService.class);
        ChartOfAccountsService chartOfAccountsService = mock(ChartOfAccountsService.class);
        AccountingPostingService accountingPostingService = mock(AccountingPostingService.class);
        FinancialAccount account =
                FinancialAccount.create(COMPANY_ID, "Cash", FinancialAccountType.CASH_REGISTER, null);
        ReflectionTestUtils.setField(account, "id", UUID.randomUUID());
        AccountingAccount adminExpense = AccountingAccount.create(
                COMPANY_ID,
                "5.1",
                "Despesas Administrativas",
                null,
                null,
                AccountingAccountType.EXPENSE,
                AccountingSystemRole.ADMIN_EXPENSE,
                true);
        ReflectionTestUtils.setField(adminExpense, "id", UUID.randomUUID());
        AccountingAccount cogs = AccountingAccount.create(
                COMPANY_ID, "4.1", "CMV", null, null, AccountingAccountType.EXPENSE, AccountingSystemRole.COGS, true);
        ReflectionTestUtils.setField(cogs, "id", UUID.randomUUID());

        when(companyFacade.resolveCompany("97.986.234/0001-91"))
                .thenReturn(new CompanySnapshot(COMPANY_ID, null, null, null, null, null, null, null, null, null));
        when(customerFacade.resolveCustomer(COMPANY_ID, "12345678901"))
                .thenReturn(new CustomerSnapshot(UUID.randomUUID(), "Customer", "12345678901", COMPANY_ID));
        when(accountRepository.findAllByCompanyId(any(), any())).thenReturn(new PageImpl<>(List.of(account)));
        when(businessNumberService.next(any())).thenReturn(1L);
        when(invoiceRepository.saveAndFlush(any())).thenAnswer(invocation -> withId(invocation.getArgument(0)));
        when(ledgerRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.saveAndFlush(any())).thenAnswer(invocation -> withId(invocation.getArgument(0)));
        when(entryRepository.saveAndFlush(any())).thenAnswer(invocation -> withId(invocation.getArgument(0)));
        when(accountingEntryRepository.countByCompanyIdAndSourceType(COMPANY_ID, AccountingEntrySourceType.MANUAL))
                .thenReturn(0L);
        when(chartOfAccountsService.requireBySystemRole(COMPANY_ID, AccountingSystemRole.ADMIN_EXPENSE))
                .thenReturn(adminExpense);
        when(chartOfAccountsService.requireBySystemRole(COMPANY_ID, AccountingSystemRole.COGS))
                .thenReturn(cogs);
        when(accountingEntryRepository.saveAndFlush(any())).thenAnswer(invocation -> withId(invocation.getArgument(0)));
        when(accountingEntryRepository.findByCompanyIdAndSourceTypeAndSourceIdAndStatus(any(), any(), any(), any()))
                .thenReturn(List.of());

        new FinancialDevSeeder(
                        invoiceRepository,
                        ledgerRepository,
                        accountRepository,
                        paymentRepository,
                        entryRepository,
                        accountingEntryRepository,
                        companyFacade,
                        customerFacade,
                        businessNumberService,
                        chartOfAccountsService,
                        accountingPostingService,
                        CLOCK)
                .run(null);

        ArgumentCaptor<Invoice> captor = ArgumentCaptor.forClass(Invoice.class);
        verify(chartOfAccountsService).ensureDefaultAccounts(COMPANY_ID);
        verify(invoiceRepository, times(FinancialDevSeeder.seedInvoiceCount())).saveAndFlush(captor.capture());
        verify(invoiceRepository, times(FinancialDevSeeder.seedInvoiceCount())).updateCreatedAtForDevSeed(any(), any());
        verify(paymentRepository, times(6)).saveAndFlush(any(Payment.class));
        verify(entryRepository, times(6)).saveAndFlush(any(FinancialEntry.class));
        verify(entryRepository, times(6)).updateOccurredAtForDevSeed(any(), any());
        verify(accountingPostingService, times(10)).postInvoiceRevenue(any(Invoice.class));
        verify(accountingEntryRepository, times(2)).saveAndFlush(any(AccountingEntry.class));
        assertThat(captor.getAllValues())
                .extracting(Invoice::getStatus)
                .contains(InvoiceStatus.OPEN, InvoiceStatus.PAID, InvoiceStatus.CANCELLED);
        assertThat(captor.getAllValues()).extracting(Invoice::getCurrencyCode).contains("BRL", "USD");
    }

    private static <T> T withId(T entity) {
        ReflectionTestUtils.setField(entity, "id", UUID.randomUUID());
        return entity;
    }
}
