package com.thiagsilvadev.erpapi.financial.internal.usecase.payment;

import static com.thiagsilvadev.erpapi.financial.api.FinancialPermissions.WRITE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ConflictException;
import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.domain.numbering.BusinessNumberService;
import com.thiagsilvadev.erpapi.common.domain.numbering.BusinessNumberType;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.financial.internal.dto.payment.CreatePaymentRequest;
import com.thiagsilvadev.erpapi.financial.internal.dto.payment.PaymentResponse;
import com.thiagsilvadev.erpapi.financial.internal.mapper.FinancialMapper;
import com.thiagsilvadev.erpapi.financial.internal.model.FinancialAccount;
import com.thiagsilvadev.erpapi.financial.internal.model.FinancialEntry;
import com.thiagsilvadev.erpapi.financial.internal.model.FinancialEntrySource;
import com.thiagsilvadev.erpapi.financial.internal.model.Invoice;
import com.thiagsilvadev.erpapi.financial.internal.model.InvoiceStatus;
import com.thiagsilvadev.erpapi.financial.internal.model.LedgerEntry;
import com.thiagsilvadev.erpapi.financial.internal.model.Payment;
import com.thiagsilvadev.erpapi.financial.internal.model.PaymentStatus;
import com.thiagsilvadev.erpapi.financial.internal.repository.FinancialAccountRepository;
import com.thiagsilvadev.erpapi.financial.internal.repository.FinancialEntryRepository;
import com.thiagsilvadev.erpapi.financial.internal.repository.InvoiceRepository;
import com.thiagsilvadev.erpapi.financial.internal.repository.LedgerEntryRepository;
import com.thiagsilvadev.erpapi.financial.internal.repository.PaymentRepository;

@Component
public class CreatePaymentUseCase {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final FinancialAccountRepository accountRepository;
    private final FinancialEntryRepository entryRepository;
    private final FinancialMapper mapper;
    private final BusinessNumberService businessNumberService;

    public CreatePaymentUseCase(
            InvoiceRepository invoiceRepository,
            PaymentRepository paymentRepository,
            LedgerEntryRepository ledgerEntryRepository,
            FinancialAccountRepository accountRepository,
            FinancialEntryRepository entryRepository,
            FinancialMapper mapper,
            BusinessNumberService businessNumberService) {
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.accountRepository = accountRepository;
        this.entryRepository = entryRepository;
        this.mapper = mapper;
        this.businessNumberService = businessNumberService;
    }

    @Transactional
    @PreAuthorize("hasAuthority('" + WRITE + "')")
    public PaymentResponse execute(CreatePaymentRequest request) {
        var companyId = TenantContext.requireTenantId();
        Invoice invoice = invoiceRepository
                .findWithLockingByIdAndCompanyId(request.invoiceId(), companyId)
                .orElseThrow(() -> new ResourceNotFoundException(Invoice.class, request.invoiceId()));
        validateInvoiceCanReceivePayment(invoice);
        validateDestinationAccount(companyId, request);

        BigDecimal paidAmount = paymentRepository.sumAmountByCompanyIdAndInvoiceId(companyId, invoice.getId());
        BigDecimal totalPaidAmount = paidAmount.add(request.amount());
        if (totalPaidAmount.compareTo(invoice.getAmount()) > 0) {
            throw new ConflictException("Payment amount exceeds the invoice balance.");
        }

        Payment payment = paymentRepository.save(Payment.create(
                businessNumberService.next(BusinessNumberType.PAYMENT),
                companyId,
                request.salesOrderId(),
                request.customerId(),
                request.invoiceId(),
                request.destinationAccountId(),
                request.amount(),
                request.method(),
                request.status(),
                request.dueDate()));

        if (request.status() == PaymentStatus.PAID) {
            payment.markAsPaid(request.paidAt() != null ? request.paidAt() : LocalDate.now());
            createIncomeEntryIfAccountPresent(payment, invoice);
        }

        if (totalPaidAmount.compareTo(invoice.getAmount()) == 0) {
            invoice.markPaid();
            ledgerEntryRepository
                    .findByCompanyIdAndInvoiceId(companyId, invoice.getId())
                    .ifPresent(LedgerEntry::settle);
        }

        return mapper.toResponse(payment);
    }

    private void validateDestinationAccount(UUID companyId, CreatePaymentRequest request) {
        if (request.status() == PaymentStatus.PAID && request.destinationAccountId() == null) {
            throw new ConflictException("Destination account is required for paid payments.");
        }
        if (request.destinationAccountId() != null) {
            if (accountRepository
                    .findByIdAndCompanyId(request.destinationAccountId(), companyId)
                    .isEmpty()) {
                throw new ResourceNotFoundException(FinancialAccount.class, request.destinationAccountId());
            }
        }
    }

    private void createIncomeEntryIfAccountPresent(Payment payment, Invoice invoice) {
        if (payment.getDestinationAccountId() != null) {
            entryRepository.save(FinancialEntry.income(
                    payment.getCompanyId(),
                    payment.getDestinationAccountId(),
                    FinancialEntrySource.PAYMENT,
                    payment.getAmount(),
                    invoice.getCurrencyCode(),
                    payment.getId(),
                    "Payment #" + payment.getPaymentNumber()));
        }
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
