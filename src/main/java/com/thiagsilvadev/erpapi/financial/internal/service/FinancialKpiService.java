package com.thiagsilvadev.erpapi.financial.internal.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.financial.api.FinancialKpiFacade;
import com.thiagsilvadev.erpapi.financial.api.FinancialKpiSnapshot;
import com.thiagsilvadev.erpapi.financial.internal.repository.FinancialEntryRepository;
import com.thiagsilvadev.erpapi.financial.internal.repository.InvoiceRepository;

@Service
class FinancialKpiService implements FinancialKpiFacade {

    private final InvoiceRepository invoiceRepository;
    private final FinancialEntryRepository entryRepository;

    FinancialKpiService(InvoiceRepository invoiceRepository, FinancialEntryRepository entryRepository) {
        this.invoiceRepository = invoiceRepository;
        this.entryRepository = entryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public FinancialKpiSnapshot load(
            UUID companyId, Instant fromInclusive, Instant toExclusive, LocalDate receivablesAsOf) {
        var invoices = invoiceRepository.findKpiRows(companyId, fromInclusive, toExclusive).stream()
                .map(row -> new FinancialKpiSnapshot.DailyInvoice(
                        row.getDate(), row.getCurrencyCode(), row.getCount(), row.getAmount()))
                .toList();
        var collections = entryRepository.findCollectionKpiRows(companyId, fromInclusive, toExclusive).stream()
                .map(row ->
                        new FinancialKpiSnapshot.DailyCollection(row.getDate(), row.getCurrencyCode(), row.getAmount()))
                .toList();
        var receivables = invoiceRepository.findReceivableKpiRows(companyId, receivablesAsOf).stream()
                .map(row -> new FinancialKpiSnapshot.Receivable(
                        row.getCurrencyCode(), row.getOpenAmount(), row.getOverdueAmount()))
                .toList();
        return new FinancialKpiSnapshot(invoices, collections, receivables);
    }
}
