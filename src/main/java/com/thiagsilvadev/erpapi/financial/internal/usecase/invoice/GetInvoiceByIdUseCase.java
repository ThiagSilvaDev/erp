package com.thiagsilvadev.erpapi.financial.internal.usecase.invoice;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialDtos.InvoiceResponse;
import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialMapper;
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
    public InvoiceResponse execute(UUID id) {
        return mapper.toResponse(
                invoiceRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(Invoice.class, id)));
    }
}
