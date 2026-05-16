package com.thiagsilvadev.erpapi.financial.internal.controller;

import java.net.URI;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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

@RestController
class InvoiceController implements InvoiceApi {

    private final InvoiceRepository invoiceRepository;
    private final FinancialEntryRepository entryRepository;
    private final CustomerFacade customerFacade;
    private final SalesFacade salesFacade;
    private final FinancialMapper mapper;

    InvoiceController(
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

    @Override
    @Transactional
    public ResponseEntity<InvoiceResponse> createInvoice(@Valid @RequestBody CreateInvoiceRequest request) {
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

        return created(invoice.getId(), mapper.toResponse(invoice));
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<InvoiceResponse> getInvoice(@PathVariable UUID id) {
        return ResponseEntity.ok(mapper.toResponse(
                invoiceRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(Invoice.class, id))));
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Page<InvoiceResponse>> listInvoices(Pageable pageable) {
        return ResponseEntity.ok(invoiceRepository.findAll(pageable).map(mapper::toResponse));
    }

    private <T> ResponseEntity<T> created(UUID id, T body) {
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.status(HttpStatus.CREATED).location(location).body(body);
    }
}
