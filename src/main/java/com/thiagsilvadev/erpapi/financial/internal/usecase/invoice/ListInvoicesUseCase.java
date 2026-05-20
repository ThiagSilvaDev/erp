package com.thiagsilvadev.erpapi.financial.internal.usecase.invoice;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialMapper;
import com.thiagsilvadev.erpapi.financial.internal.dto.invoice.InvoiceResponse;
import com.thiagsilvadev.erpapi.financial.internal.dto.invoice.InvoiceSearchCriteria;
import com.thiagsilvadev.erpapi.financial.internal.repository.InvoiceRepository;

@Component
public class ListInvoicesUseCase {

    private final InvoiceRepository invoiceRepository;
    private final FinancialMapper mapper;

    public ListInvoicesUseCase(InvoiceRepository invoiceRepository, FinancialMapper mapper) {
        this.invoiceRepository = invoiceRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.financial.api.FinancialPermissions).READ)")
    public Page<InvoiceResponse> execute(InvoiceSearchCriteria criteria, Pageable pageable) {
        if (criteria != null && criteria.salesOrderId() != null) {
            return invoiceRepository
                    .findBySalesOrderId(criteria.salesOrderId(), pageable)
                    .map(mapper::toResponse);
        }
        return invoiceRepository.findAll(pageable).map(mapper::toResponse);
    }
}
