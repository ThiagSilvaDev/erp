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
import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialDtos.CreatePaymentRequest;
import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialDtos.PaymentResponse;
import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialMapper;
import com.thiagsilvadev.erpapi.financial.internal.model.FinancialEntry;
import com.thiagsilvadev.erpapi.financial.internal.model.Invoice;
import com.thiagsilvadev.erpapi.financial.internal.model.Payment;
import com.thiagsilvadev.erpapi.financial.internal.repository.FinancialEntryRepository;
import com.thiagsilvadev.erpapi.financial.internal.repository.InvoiceRepository;
import com.thiagsilvadev.erpapi.financial.internal.repository.PaymentRepository;

@RestController
class PaymentController implements PaymentApi {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final FinancialEntryRepository entryRepository;
    private final FinancialMapper mapper;

    PaymentController(
            InvoiceRepository invoiceRepository,
            PaymentRepository paymentRepository,
            FinancialEntryRepository entryRepository,
            FinancialMapper mapper) {
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.entryRepository = entryRepository;
        this.mapper = mapper;
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
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable UUID id) {
        return ResponseEntity.ok(mapper.toResponse(
                paymentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(Payment.class, id))));
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Page<PaymentResponse>> listPayments(Pageable pageable) {
        return ResponseEntity.ok(paymentRepository.findAll(pageable).map(mapper::toResponse));
    }

    private <T> ResponseEntity<T> created(UUID id, T body) {
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.status(HttpStatus.CREATED).location(location).body(body);
    }
}
