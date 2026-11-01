package com.thiagsilvadev.erpapi.financial.internal.usecase.payment;

import static com.thiagsilvadev.erpapi.financial.api.FinancialPermissions.READ;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialMapper;
import com.thiagsilvadev.erpapi.financial.internal.dto.payment.PaymentResponse;
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
    @PreAuthorize("hasAuthority('" + READ + "')")
    public PaymentResponse execute(UUID id) {
        var companyId = TenantContext.requireTenantId();
        return mapper.toResponse(paymentRepository
                .findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException(Payment.class, id)));
    }
}
