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

import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialDtos.CreatePaymentRequest;
import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialDtos.PaymentResponse;
import com.thiagsilvadev.erpapi.financial.internal.usecase.payment.CreatePaymentUseCase;
import com.thiagsilvadev.erpapi.financial.internal.usecase.payment.GetPaymentByIdUseCase;
import com.thiagsilvadev.erpapi.financial.internal.usecase.payment.ListPaymentsUseCase;

@RestController
class PaymentController implements PaymentApi {

    private final CreatePaymentUseCase createPaymentUseCase;
    private final GetPaymentByIdUseCase getPaymentByIdUseCase;
    private final ListPaymentsUseCase listPaymentsUseCase;

    PaymentController(
            CreatePaymentUseCase createPaymentUseCase,
            GetPaymentByIdUseCase getPaymentByIdUseCase,
            ListPaymentsUseCase listPaymentsUseCase) {
        this.createPaymentUseCase = createPaymentUseCase;
        this.getPaymentByIdUseCase = getPaymentByIdUseCase;
        this.listPaymentsUseCase = listPaymentsUseCase;
    }

    @Override
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        PaymentResponse response = createPaymentUseCase.execute(request);
        return created(response.id(), response);
    }

    @Override
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable UUID id) {
        return ResponseEntity.ok(getPaymentByIdUseCase.execute(id));
    }

    @Override
    public ResponseEntity<Page<PaymentResponse>> listPayments(Pageable pageable) {
        return ResponseEntity.ok(listPaymentsUseCase.execute(pageable));
    }

    private <T> ResponseEntity<T> created(UUID id, T body) {
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.status(HttpStatus.CREATED).location(location).body(body);
    }
}
