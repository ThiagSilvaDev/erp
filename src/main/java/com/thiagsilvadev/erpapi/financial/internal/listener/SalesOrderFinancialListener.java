package com.thiagsilvadev.erpapi.financial.internal.listener;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import com.thiagsilvadev.erpapi.common.domain.exception.ConflictException;
import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.domain.numbering.BusinessNumberService;
import com.thiagsilvadev.erpapi.common.domain.numbering.BusinessNumberType;
import com.thiagsilvadev.erpapi.financial.internal.model.BillingPartyType;
import com.thiagsilvadev.erpapi.financial.internal.model.Invoice;
import com.thiagsilvadev.erpapi.financial.internal.model.LedgerEntry;
import com.thiagsilvadev.erpapi.financial.internal.repository.InvoiceRepository;
import com.thiagsilvadev.erpapi.financial.internal.repository.LedgerEntryRepository;
import com.thiagsilvadev.erpapi.financial.internal.repository.PaymentRepository;
import com.thiagsilvadev.erpapi.sales.api.SaleType;
import com.thiagsilvadev.erpapi.sales.api.SalesFacade;
import com.thiagsilvadev.erpapi.sales.api.SalesOrderCancelledEvent;
import com.thiagsilvadev.erpapi.sales.api.SalesOrderConfirmedEvent;
import com.thiagsilvadev.erpapi.sales.api.SalesOrderSnapshot;

@Component
public class SalesOrderFinancialListener {

    private static final int SALE_INVOICE_DUE_DAYS = 15;

    private final InvoiceRepository invoiceRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final PaymentRepository paymentRepository;
    private final BusinessNumberService businessNumberService;
    private final SalesFacade salesFacade;

    public SalesOrderFinancialListener(
            InvoiceRepository invoiceRepository,
            LedgerEntryRepository ledgerEntryRepository,
            PaymentRepository paymentRepository,
            BusinessNumberService businessNumberService,
            SalesFacade salesFacade) {
        this.invoiceRepository = invoiceRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.paymentRepository = paymentRepository;
        this.businessNumberService = businessNumberService;
        this.salesFacade = salesFacade;
    }

    @ApplicationModuleListener
    public void on(SalesOrderConfirmedEvent event) {
        if (invoiceRepository.existsBySalesOrderId(event.salesOrderId())) {
            return;
        }

        SalesOrderSnapshot order = salesFacade
                .findOrderById(event.salesOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("SalesOrder", event.salesOrderId()));

        Invoice invoice = invoiceRepository.save(Invoice.create(
                businessNumberService.next(BusinessNumberType.INVOICE),
                order.sellerCompanyId(),
                order.saleType() == SaleType.B2B ? BillingPartyType.COMPANY : BillingPartyType.CUSTOMER,
                order.saleType() == SaleType.B2B ? order.buyerCompanyId() : order.customerId(),
                event.salesOrderId(),
                order.totalAmount(),
                order.currencyCode(),
                event.confirmedAt().plusDays(SALE_INVOICE_DUE_DAYS)));

        ledgerEntryRepository.save(LedgerEntry.receivable(
                invoice.getCompanyId(),
                invoice.getId(),
                invoice.getAmount(),
                invoice.getCurrencyCode(),
                invoice.getDueDate()));
    }

    @ApplicationModuleListener
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
