package com.thiagsilvadev.erpapi.financial.internal.usecase.invoice;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ConflictException;
import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.customer.api.CustomerFacade;
import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialDtos.CreateInvoiceRequest;
import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialDtos.InvoiceResponse;
import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialMapper;
import com.thiagsilvadev.erpapi.financial.internal.model.Invoice;
import com.thiagsilvadev.erpapi.financial.internal.model.LedgerEntry;
import com.thiagsilvadev.erpapi.financial.internal.repository.InvoiceRepository;
import com.thiagsilvadev.erpapi.financial.internal.repository.LedgerEntryRepository;
import com.thiagsilvadev.erpapi.sales.api.OrderStatus;
import com.thiagsilvadev.erpapi.sales.api.SalesFacade;
import com.thiagsilvadev.erpapi.sales.api.SalesOrderSnapshot;

@Component
public class CreateInvoiceUseCase {

    private final InvoiceRepository invoiceRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final CustomerFacade customerFacade;
    private final SalesFacade salesFacade;
    private final FinancialMapper mapper;

    public CreateInvoiceUseCase(
            InvoiceRepository invoiceRepository,
            LedgerEntryRepository ledgerEntryRepository,
            CustomerFacade customerFacade,
            SalesFacade salesFacade,
            FinancialMapper mapper) {
        this.invoiceRepository = invoiceRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.customerFacade = customerFacade;
        this.salesFacade = salesFacade;
        this.mapper = mapper;
    }

    @Transactional
    @PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.financial.api.FinancialPermissions).WRITE)")
    public InvoiceResponse execute(CreateInvoiceRequest request) {
        customerFacade
                .findCustomerById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException(Object.class, request.customerId()));

        if (request.salesOrderId() != null) {
            SalesOrderSnapshot order = salesFacade
                    .findOrderById(request.salesOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException(Object.class, request.salesOrderId()));
            validateSalesOrderInvoice(request, order);
        }

        Invoice invoice = invoiceRepository.save(Invoice.create(
                request.customerId(),
                request.salesOrderId(),
                request.amount(),
                request.currencyCode(),
                request.dueDate()));

        ledgerEntryRepository.save(LedgerEntry.receivable(
                invoice.getId(), invoice.getAmount(), invoice.getCurrencyCode(), invoice.getDueDate()));

        return mapper.toResponse(invoice);
    }

    private void validateSalesOrderInvoice(CreateInvoiceRequest request, SalesOrderSnapshot order) {
        if (order.status() != OrderStatus.CONFIRMED) {
            throw new ConflictException("Only confirmed sales orders can be invoiced.");
        }
        if (!order.customerId().equals(request.customerId())) {
            throw new ConflictException("Invoice customer must match the sales order customer.");
        }
        if (order.totalAmount().compareTo(request.amount()) != 0) {
            throw new ConflictException("Invoice amount must match the sales order total.");
        }
        if (!order.currencyCode().equalsIgnoreCase(request.currencyCode())) {
            throw new ConflictException("Invoice currency must match the sales order currency.");
        }
        if (invoiceRepository.existsBySalesOrderId(order.id())) {
            throw new ConflictException("Sales order already has an invoice.");
        }
    }
}
