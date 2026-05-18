package com.thiagsilvadev.erpapi.financial.internal.usecase.payment;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ConflictException;
import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialDtos.CreatePaymentRequest;
import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialDtos.PaymentResponse;
import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialMapper;
import com.thiagsilvadev.erpapi.financial.internal.model.Invoice;
import com.thiagsilvadev.erpapi.financial.internal.model.InvoiceStatus;
import com.thiagsilvadev.erpapi.financial.internal.model.LedgerEntry;
import com.thiagsilvadev.erpapi.financial.internal.model.Payment;
import com.thiagsilvadev.erpapi.financial.internal.repository.InvoiceRepository;
import com.thiagsilvadev.erpapi.financial.internal.repository.LedgerEntryRepository;
import com.thiagsilvadev.erpapi.financial.internal.repository.PaymentRepository;

@Component
public class CreatePaymentUseCase {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final FinancialMapper mapper;

    public CreatePaymentUseCase(
            InvoiceRepository invoiceRepository,
            PaymentRepository paymentRepository,
            LedgerEntryRepository ledgerEntryRepository,
            FinancialMapper mapper) {
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.mapper = mapper;
    }

    @Transactional
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.financial.api.FinancialPermissions).WRITE)")
    public PaymentResponse execute(CreatePaymentRequest request) {
        Invoice invoice = invoiceRepository
                .findWithLockingById(request.invoiceId())
                .orElseThrow(() -> new ResourceNotFoundException(Invoice.class, request.invoiceId()));
        validateInvoiceCanReceivePayment(invoice);

        BigDecimal paidAmount = paymentRepository.sumAmountByInvoiceId(invoice.getId());
        BigDecimal totalPaidAmount = paidAmount.add(request.amount());
        if (totalPaidAmount.compareTo(invoice.getAmount()) > 0) {
            throw new ConflictException("Payment amount exceeds the invoice balance.");
        }

        Payment payment =
                paymentRepository.save(Payment.create(request.invoiceId(), request.amount(), request.paidAt()));

        if (totalPaidAmount.compareTo(invoice.getAmount()) == 0) {
            invoice.markPaid();
            ledgerEntryRepository.findByInvoiceId(invoice.getId()).ifPresent(LedgerEntry::settle);
        }

        return mapper.toResponse(payment);
    }

    private static void validateInvoiceCanReceivePayment(Invoice invoice) {
        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new ConflictException("Cancelled invoices cannot receive payments.");
        }
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new ConflictException("Paid invoices cannot receive payments.");
        }
    }
}
