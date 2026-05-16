package com.thiagsilvadev.erpapi.financial.internal.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.thiagsilvadev.erpapi.common.web.ResponseEntities;
import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialDtos.CreateInvoiceRequest;
import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialDtos.InvoiceResponse;
import com.thiagsilvadev.erpapi.financial.internal.usecase.invoice.CreateInvoiceUseCase;
import com.thiagsilvadev.erpapi.financial.internal.usecase.invoice.GetInvoiceByIdUseCase;
import com.thiagsilvadev.erpapi.financial.internal.usecase.invoice.ListInvoicesUseCase;

@RestController
class InvoiceController implements InvoiceApi {

    private final CreateInvoiceUseCase createInvoiceUseCase;
    private final GetInvoiceByIdUseCase getInvoiceByIdUseCase;
    private final ListInvoicesUseCase listInvoicesUseCase;

    InvoiceController(
            CreateInvoiceUseCase createInvoiceUseCase,
            GetInvoiceByIdUseCase getInvoiceByIdUseCase,
            ListInvoicesUseCase listInvoicesUseCase) {
        this.createInvoiceUseCase = createInvoiceUseCase;
        this.getInvoiceByIdUseCase = getInvoiceByIdUseCase;
        this.listInvoicesUseCase = listInvoicesUseCase;
    }

    @Override
    public ResponseEntity<InvoiceResponse> createInvoice(@Valid @RequestBody CreateInvoiceRequest request) {
        InvoiceResponse response = createInvoiceUseCase.execute(request);
        return ResponseEntities.created(response.id(), response);
    }

    @Override
    public InvoiceResponse getInvoice(@PathVariable UUID id) {
        return getInvoiceByIdUseCase.execute(id);
    }

    @Override
    public Page<InvoiceResponse> listInvoices(Pageable pageable) {
        return listInvoicesUseCase.execute(pageable);
    }
}
