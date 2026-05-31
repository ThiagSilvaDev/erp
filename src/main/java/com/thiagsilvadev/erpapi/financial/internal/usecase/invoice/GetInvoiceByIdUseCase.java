package com.thiagsilvadev.erpapi.financial.internal.usecase.invoice;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialMapper;
import com.thiagsilvadev.erpapi.financial.internal.dto.invoice.InvoiceResponse;
import com.thiagsilvadev.erpapi.financial.internal.model.Invoice;
import com.thiagsilvadev.erpapi.financial.internal.repository.InvoiceRepository;

@Component
public class GetInvoiceByIdUseCase {

    private final InvoiceRepository invoiceRepository;
    private final FinancialMapper mapper;

    public GetInvoiceByIdUseCase(InvoiceRepository invoiceRepository, FinancialMapper mapper) {
        this.invoiceRepository = invoiceRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.financial.api.FinancialPermissions).READ)")
    public InvoiceResponse execute(UUID id) {
        var companyId = TenantContext.requireTenantId();
        return mapper.toResponse(invoiceRepository
                .findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException(Invoice.class, id)));
    }
}
