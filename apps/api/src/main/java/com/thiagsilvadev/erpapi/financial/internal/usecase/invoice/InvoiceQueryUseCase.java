package com.thiagsilvadev.erpapi.financial.internal.usecase.invoice;

import static com.thiagsilvadev.erpapi.financial.api.FinancialPermissions.READ;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.financial.internal.dto.invoice.InvoiceResponse;
import com.thiagsilvadev.erpapi.financial.internal.dto.invoice.InvoiceSearchCriteria;
import com.thiagsilvadev.erpapi.financial.internal.mapper.FinancialMapper;
import com.thiagsilvadev.erpapi.financial.internal.model.Invoice;
import com.thiagsilvadev.erpapi.financial.internal.repository.InvoiceRepository;

@Component
public class InvoiceQueryUseCase {

    private final InvoiceRepository invoiceRepository;
    private final FinancialMapper mapper;

    public InvoiceQueryUseCase(InvoiceRepository invoiceRepository, FinancialMapper mapper) {
        this.invoiceRepository = invoiceRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + READ + "')")
    public InvoiceResponse getById(UUID id) {
        var companyId = TenantContext.requireTenantId();
        return mapper.toResponse(invoiceRepository
                .findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException(Invoice.class, id)));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + READ + "')")
    public Page<InvoiceResponse> list(InvoiceSearchCriteria criteria, Pageable pageable) {
        var companyId = TenantContext.requireTenantId();
        if (criteria != null && criteria.salesOrderId() != null) {
            return invoiceRepository
                    .findByCompanyIdAndSalesOrderId(companyId, criteria.salesOrderId(), pageable)
                    .map(mapper::toResponse);
        }
        return invoiceRepository.findAllByCompanyId(companyId, pageable).map(mapper::toResponse);
    }
}
