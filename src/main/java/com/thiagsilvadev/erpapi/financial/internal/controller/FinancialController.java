package com.thiagsilvadev.erpapi.financial.internal.controller;

import java.net.URI;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.customer.api.CustomerFacade;
import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialDtos.*;
import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialMapper;
import com.thiagsilvadev.erpapi.financial.internal.model.*;
import com.thiagsilvadev.erpapi.financial.internal.repository.*;
import com.thiagsilvadev.erpapi.sales.api.SalesFacade;

@RestController
class FinancialController implements FinancialApi {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final FinancialEntryRepository entryRepository;
    private final CustomerFacade customerFacade;
    private final SalesFacade salesFacade;
    private final FinancialMapper mapper;

    FinancialController(
            InvoiceRepository invoiceRepository,
            PaymentRepository paymentRepository,
            FinancialEntryRepository entryRepository,
            CustomerFacade customerFacade,
            SalesFacade salesFacade,
            FinancialMapper mapper) {
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
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
    public InvoiceResponse getInvoice(@PathVariable UUID id) {
        return mapper.toResponse(
                invoiceRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(Invoice.class, id)));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InvoiceResponse> listInvoices(Pageable pageable) {
        return invoiceRepository.findAll(pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        Invoice invoice = invoiceRepository
                .findById(request.invoiceId())
                .orElseThrow(() -> new ResourceNotFoundException(Invoice.class, request.invoiceId()));
        Payment payment =
                paymentRepository.save(Payment.create(request.invoiceId(), request.amount(), request.paidAt()));
        if (request.amount().compareTo(invoice.getAmount()) >= 0) {
            invoice.markPaid();
            entryRepository.findByInvoiceId(invoice.getId()).ifPresent(FinancialEntry::settle);
        }
        return created(payment.getId(), mapper.toResponse(payment));
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(@PathVariable UUID id) {
        return mapper.toResponse(
                paymentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(Payment.class, id)));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> listPayments(Pageable pageable) {
        return paymentRepository.findAll(pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public FinancialEntryResponse getEntry(@PathVariable UUID id) {
        return mapper.toResponse(entryRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(FinancialEntry.class, id)));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FinancialEntryResponse> listEntries(Pageable pageable) {
        return entryRepository.findAll(pageable).map(mapper::toResponse);
    }

    private <T> ResponseEntity<T> created(UUID id, T body) {
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.status(HttpStatus.CREATED).location(location).body(body);
    }
}
