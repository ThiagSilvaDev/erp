package com.thiagsilvadev.erpapi.financial.internal.usecase.payment;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialDtos.CreatePaymentRequest;
import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialDtos.PaymentResponse;
import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialMapper;
import com.thiagsilvadev.erpapi.financial.internal.model.FinancialEntry;
import com.thiagsilvadev.erpapi.financial.internal.model.Invoice;
import com.thiagsilvadev.erpapi.financial.internal.model.Payment;
import com.thiagsilvadev.erpapi.financial.internal.repository.FinancialEntryRepository;
import com.thiagsilvadev.erpapi.financial.internal.repository.InvoiceRepository;
import com.thiagsilvadev.erpapi.financial.internal.repository.PaymentRepository;

@Component
public class CreatePaymentUseCase {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final FinancialEntryRepository entryRepository;
    private final FinancialMapper mapper;

    public CreatePaymentUseCase(
            InvoiceRepository invoiceRepository,
            PaymentRepository paymentRepository,
            FinancialEntryRepository entryRepository,
            FinancialMapper mapper) {
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.entryRepository = entryRepository;
        this.mapper = mapper;
    }

    @Transactional
    public PaymentResponse execute(CreatePaymentRequest request) {
        Invoice invoice = invoiceRepository
                .findById(request.invoiceId())
                .orElseThrow(() -> new ResourceNotFoundException(Invoice.class, request.invoiceId()));

        Payment payment =
                paymentRepository.save(Payment.create(request.invoiceId(), request.amount(), request.paidAt()));

        if (request.amount().compareTo(invoice.getAmount()) >= 0) {
            invoice.markPaid();
            entryRepository.findByInvoiceId(invoice.getId()).ifPresent(FinancialEntry::settle);
        }

        return mapper.toResponse(payment);
    }
}
