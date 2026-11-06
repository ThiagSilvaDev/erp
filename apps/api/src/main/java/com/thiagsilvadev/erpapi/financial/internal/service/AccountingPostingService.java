package com.thiagsilvadev.erpapi.financial.internal.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ConflictException;
import com.thiagsilvadev.erpapi.financial.internal.model.*;
import com.thiagsilvadev.erpapi.financial.internal.repository.AccountingEntryRepository;

@Service
public class AccountingPostingService {

    private final AccountingEntryRepository entryRepository;
    private final ChartOfAccountsService chartOfAccountsService;
    private final Clock clock;

    AccountingPostingService(
            AccountingEntryRepository entryRepository, ChartOfAccountsService chartOfAccountsService, Clock clock) {
        this.entryRepository = entryRepository;
        this.chartOfAccountsService = chartOfAccountsService;
        this.clock = clock;
    }

    @Transactional
    public void postInvoiceRevenue(Invoice invoice) {
        chartOfAccountsService.ensureDefaultAccounts(invoice.getCompanyId());
        AccountingAccount revenueAccount =
                chartOfAccountsService.requireBySystemRole(invoice.getCompanyId(), AccountingSystemRole.REVENUE_SALES);
        if (!revenueAccount.canAcceptPostings()) {
            throw new ConflictException("Revenue sales accounting account cannot accept postings.");
        }

        boolean alreadyPosted = !entryRepository
                .findByCompanyIdAndSourceTypeAndSourceIdAndStatus(
                        invoice.getCompanyId(),
                        AccountingEntrySourceType.INVOICE,
                        invoice.getId(),
                        AccountingEntryStatus.POSTED)
                .isEmpty();
        if (alreadyPosted) {
            return;
        }

        entryRepository.save(AccountingEntry.create(
                invoice.getCompanyId(),
                revenueAccount.getId(),
                LocalDate.now(clock.withZone(ZoneOffset.UTC)),
                invoice.getAmount(),
                invoice.getCurrencyCode(),
                AccountingEntryDirection.CREDIT,
                AccountingEntrySourceType.INVOICE,
                invoice.getId(),
                "Invoice " + invoice.getInvoiceNumber()));
    }

    @Transactional
    public void reverseInvoiceRevenue(Invoice invoice) {
        List<AccountingEntry> posted = entryRepository.findByCompanyIdAndSourceTypeAndSourceIdAndStatus(
                invoice.getCompanyId(),
                AccountingEntrySourceType.INVOICE,
                invoice.getId(),
                AccountingEntryStatus.POSTED);
        LocalDate reverseDate = LocalDate.now(clock.withZone(ZoneOffset.UTC));
        for (AccountingEntry entry : posted) {
            AccountingEntry reversal = entry.reverse(reverseDate, "Reversal of invoice " + invoice.getInvoiceNumber());
            entryRepository.save(entry);
            entryRepository.save(reversal);
        }
    }
}
