package com.thiagsilvadev.erpapi.financial.internal.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.thiagsilvadev.erpapi.common.web.ResponseEntities;
import com.thiagsilvadev.erpapi.financial.internal.dto.invoice.CreateInvoiceRequest;
import com.thiagsilvadev.erpapi.financial.internal.dto.invoice.InvoiceResponse;
import com.thiagsilvadev.erpapi.financial.internal.dto.invoice.InvoiceSearchCriteria;
import com.thiagsilvadev.erpapi.financial.internal.usecase.invoice.CreateInvoiceUseCase;
import com.thiagsilvadev.erpapi.financial.internal.usecase.invoice.InvoiceQueryUseCase;

@RestController
class InvoiceController implements InvoiceApi {

    private final CreateInvoiceUseCase createInvoiceUseCase;
    private final InvoiceQueryUseCase invoiceQueryUseCase;

    InvoiceController(CreateInvoiceUseCase createInvoiceUseCase, InvoiceQueryUseCase invoiceQueryUseCase) {
        this.createInvoiceUseCase = createInvoiceUseCase;
        this.invoiceQueryUseCase = invoiceQueryUseCase;
    }

    @Override
    public ResponseEntity<InvoiceResponse> create(@Valid @RequestBody CreateInvoiceRequest request) {
        InvoiceResponse response = createInvoiceUseCase.execute(request);
        return ResponseEntities.created(response.id(), response);
    }

    @Override
    public InvoiceResponse getById(@PathVariable UUID id) {
        return invoiceQueryUseCase.getById(id);
    }

    @Override
    public Page<InvoiceResponse> list(InvoiceSearchCriteria criteria, Pageable pageable) {
        return invoiceQueryUseCase.list(criteria, pageable);
    }
}
