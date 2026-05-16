package com.thiagsilvadev.erpapi.financial.internal.controller;

import java.net.URI;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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
        return created(response.id(), response);
    }

    @Override
    public ResponseEntity<InvoiceResponse> getInvoice(@PathVariable UUID id) {
        return ResponseEntity.ok(getInvoiceByIdUseCase.execute(id));
    }

    @Override
    public ResponseEntity<Page<InvoiceResponse>> listInvoices(Pageable pageable) {
        return ResponseEntity.ok(listInvoicesUseCase.execute(pageable));
    }

    private <T> ResponseEntity<T> created(UUID id, T body) {
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.status(HttpStatus.CREATED).location(location).body(body);
    }
}
