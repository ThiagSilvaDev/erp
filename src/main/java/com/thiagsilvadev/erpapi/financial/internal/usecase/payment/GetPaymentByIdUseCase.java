package com.thiagsilvadev.erpapi.financial.internal.usecase.payment;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialDtos.PaymentResponse;
import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialMapper;
import com.thiagsilvadev.erpapi.financial.internal.model.Payment;
import com.thiagsilvadev.erpapi.financial.internal.repository.PaymentRepository;

@Component
public class GetPaymentByIdUseCase {

    private final PaymentRepository paymentRepository;
    private final FinancialMapper mapper;

    public GetPaymentByIdUseCase(PaymentRepository paymentRepository, FinancialMapper mapper) {
        this.paymentRepository = paymentRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public PaymentResponse execute(UUID id) {
        return mapper.toResponse(
                paymentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(Payment.class, id)));
    }
}
