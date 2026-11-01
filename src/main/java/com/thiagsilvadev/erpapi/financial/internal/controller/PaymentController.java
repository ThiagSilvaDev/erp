package com.thiagsilvadev.erpapi.financial.internal.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.thiagsilvadev.erpapi.common.web.ResponseEntities;
import com.thiagsilvadev.erpapi.financial.internal.dto.payment.CreatePaymentRequest;
import com.thiagsilvadev.erpapi.financial.internal.dto.payment.PaymentResponse;
import com.thiagsilvadev.erpapi.financial.internal.usecase.payment.CreatePaymentUseCase;
import com.thiagsilvadev.erpapi.financial.internal.usecase.payment.PaymentQueryUseCase;

@RestController
class PaymentController implements PaymentApi {

    private final CreatePaymentUseCase createPaymentUseCase;
    private final PaymentQueryUseCase paymentQueryUseCase;

    PaymentController(CreatePaymentUseCase createPaymentUseCase, PaymentQueryUseCase paymentQueryUseCase) {
        this.createPaymentUseCase = createPaymentUseCase;
        this.paymentQueryUseCase = paymentQueryUseCase;
    }

    @Override
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        PaymentResponse response = createPaymentUseCase.execute(request);
        return ResponseEntities.created(response.id(), response);
    }

    @Override
    public PaymentResponse getPayment(@PathVariable UUID id) {
        return paymentQueryUseCase.getById(id);
    }

    @Override
    public Page<PaymentResponse> listPayments(Pageable pageable) {
        return paymentQueryUseCase.list(pageable);
    }
}
