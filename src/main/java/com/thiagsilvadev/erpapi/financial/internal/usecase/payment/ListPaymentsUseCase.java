package com.thiagsilvadev.erpapi.financial.internal.usecase.payment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialDtos.PaymentResponse;
import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialMapper;
import com.thiagsilvadev.erpapi.financial.internal.repository.PaymentRepository;

@Component
public class ListPaymentsUseCase {

    private final PaymentRepository paymentRepository;
    private final FinancialMapper mapper;

    public ListPaymentsUseCase(PaymentRepository paymentRepository, FinancialMapper mapper) {
        this.paymentRepository = paymentRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.financial.api.FinancialPermissions).READ)")
    public Page<PaymentResponse> execute(Pageable pageable) {
        return paymentRepository.findAll(pageable).map(mapper::toResponse);
    }
}
