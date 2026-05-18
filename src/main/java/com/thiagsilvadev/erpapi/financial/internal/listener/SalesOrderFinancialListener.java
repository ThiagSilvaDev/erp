package com.thiagsilvadev.erpapi.financial.internal.listener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ConflictException;
import com.thiagsilvadev.erpapi.common.domain.numbering.BusinessNumberService;
import com.thiagsilvadev.erpapi.common.domain.numbering.BusinessNumberType;
import com.thiagsilvadev.erpapi.financial.internal.model.Invoice;
import com.thiagsilvadev.erpapi.financial.internal.model.LedgerEntry;
import com.thiagsilvadev.erpapi.financial.internal.repository.InvoiceRepository;
import com.thiagsilvadev.erpapi.financial.internal.repository.LedgerEntryRepository;
import com.thiagsilvadev.erpapi.financial.internal.repository.PaymentRepository;
import com.thiagsilvadev.erpapi.sales.api.SalesOrderCancelledEvent;
import com.thiagsilvadev.erpapi.sales.api.SalesOrderConfirmedEvent;

@Component
public class SalesOrderFinancialListener {

    private static final int SALE_INVOICE_DUE_DAYS = 15;

    private final InvoiceRepository invoiceRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final PaymentRepository paymentRepository;
    private final BusinessNumberService businessNumberService;

    public SalesOrderFinancialListener(
            InvoiceRepository invoiceRepository,
            LedgerEntryRepository ledgerEntryRepository,
            PaymentRepository paymentRepository,
            BusinessNumberService businessNumberService) {
        this.invoiceRepository = invoiceRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.paymentRepository = paymentRepository;
        this.businessNumberService = businessNumberService;
    }

    @EventListener
    @Transactional
    public void on(SalesOrderConfirmedEvent event) {
        if (invoiceRepository.existsBySalesOrderId(event.salesOrderId())) {
            return;
        }

        Invoice invoice = invoiceRepository.save(Invoice.create(
                businessNumberService.next(BusinessNumberType.INVOICE),
                event.customerId(),
                event.salesOrderId(),
                event.totalAmount(),
                event.currencyCode(),
                event.confirmedAt().plusDays(SALE_INVOICE_DUE_DAYS)));

        ledgerEntryRepository.save(LedgerEntry.receivable(
                invoice.getId(), invoice.getAmount(), invoice.getCurrencyCode(), invoice.getDueDate()));
    }

    @EventListener
    @Transactional
    public void on(SalesOrderCancelledEvent event) {
        invoiceRepository.findWithLockingBySalesOrderId(event.salesOrderId()).ifPresent(invoice -> {
            if (paymentRepository.existsByInvoiceId(invoice.getId())) {
                throw new ConflictException("Cannot cancel sales order with financial payments.");
            }
            invoice.cancel();
            ledgerEntryRepository.findByInvoiceId(invoice.getId()).ifPresent(LedgerEntry::cancel);
        });
    }
}
