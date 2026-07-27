package com.thiagsilvadev.erpapi.financial.internal.service;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.reporting.ComparativePeriod;
import com.thiagsilvadev.erpapi.financial.api.FinancialKpiFacade;
import com.thiagsilvadev.erpapi.financial.api.FinancialKpiSnapshot;
import com.thiagsilvadev.erpapi.financial.internal.repository.FinancialEntryRepository;
import com.thiagsilvadev.erpapi.financial.internal.repository.InvoiceRepository;

@Service
class FinancialKpiService implements FinancialKpiFacade {

    private final InvoiceRepository invoiceRepository;
    private final FinancialEntryRepository entryRepository;
    private final FinancialKpiCalculator calculator;

    FinancialKpiService(
            InvoiceRepository invoiceRepository,
            FinancialEntryRepository entryRepository,
            FinancialKpiCalculator calculator) {
        this.invoiceRepository = invoiceRepository;
        this.entryRepository = entryRepository;
        this.calculator = calculator;
    }

    @Override
    @Transactional(readOnly = true)
    public FinancialKpiSnapshot calculate(UUID companyId, ComparativePeriod period, LocalDate receivablesAsOf) {
        var invoices =
                invoiceRepository
                        .findKpiRows(companyId, period.queryFromInclusive(), period.queryToExclusive())
                        .stream()
                        .map(row -> new FinancialKpiCalculator.InvoiceRow(
                                row.getDate(), row.getCurrencyCode(), row.getCount(), row.getAmount()))
                        .toList();
        var collections =
                entryRepository
                        .findCollectionKpiRows(companyId, period.queryFromInclusive(), period.queryToExclusive())
                        .stream()
                        .map(row -> new FinancialKpiCalculator.CollectionRow(
                                row.getDate(), row.getCurrencyCode(), row.getAmount()))
                        .toList();
        var receivables = invoiceRepository.findReceivableKpiRows(companyId, receivablesAsOf).stream()
                .map(row -> new FinancialKpiCalculator.ReceivableRow(
                        row.getCurrencyCode(), row.getOpenAmount(), row.getOverdueAmount()))
                .toList();
        return calculator.calculate(invoices, collections, receivables, period);
    }
}
