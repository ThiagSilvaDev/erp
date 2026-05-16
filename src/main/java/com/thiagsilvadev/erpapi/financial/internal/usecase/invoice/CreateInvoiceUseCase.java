package com.thiagsilvadev.erpapi.financial.internal.usecase.invoice;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.customer.api.CustomerFacade;
import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialDtos.CreateInvoiceRequest;
import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialDtos.InvoiceResponse;
import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialMapper;
import com.thiagsilvadev.erpapi.financial.internal.model.FinancialEntry;
import com.thiagsilvadev.erpapi.financial.internal.model.Invoice;
import com.thiagsilvadev.erpapi.financial.internal.repository.FinancialEntryRepository;
import com.thiagsilvadev.erpapi.financial.internal.repository.InvoiceRepository;
import com.thiagsilvadev.erpapi.sales.api.SalesFacade;

@Component
public class CreateInvoiceUseCase {

    private final InvoiceRepository invoiceRepository;
    private final FinancialEntryRepository entryRepository;
    private final CustomerFacade customerFacade;
    private final SalesFacade salesFacade;
    private final FinancialMapper mapper;

    public CreateInvoiceUseCase(
            InvoiceRepository invoiceRepository,
            FinancialEntryRepository entryRepository,
            CustomerFacade customerFacade,
            SalesFacade salesFacade,
            FinancialMapper mapper) {
        this.invoiceRepository = invoiceRepository;
        this.entryRepository = entryRepository;
        this.customerFacade = customerFacade;
        this.salesFacade = salesFacade;
        this.mapper = mapper;
    }

    @Transactional
    public InvoiceResponse execute(CreateInvoiceRequest request) {
        customerFacade
                .findCustomerById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException(Object.class, request.customerId()));

        if (request.salesOrderId() != null) {
            salesFacade
                    .findOrderById(request.salesOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException(Object.class, request.salesOrderId()));
        }

        Invoice invoice = invoiceRepository.save(Invoice.create(
                request.customerId(),
                request.salesOrderId(),
                request.amount(),
                request.currencyCode(),
                request.dueDate()));

        entryRepository.save(FinancialEntry.receivable(
                invoice.getId(), invoice.getAmount(), invoice.getCurrencyCode(), invoice.getDueDate()));

        return mapper.toResponse(invoice);
    }
}
