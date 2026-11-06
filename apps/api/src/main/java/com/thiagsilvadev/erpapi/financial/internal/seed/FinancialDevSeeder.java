package com.thiagsilvadev.erpapi.financial.internal.seed;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.numbering.BusinessNumberService;
import com.thiagsilvadev.erpapi.common.domain.numbering.BusinessNumberType;
import com.thiagsilvadev.erpapi.company.api.CompanyFacade;
import com.thiagsilvadev.erpapi.customer.api.CustomerFacade;
import com.thiagsilvadev.erpapi.financial.internal.model.*;
import com.thiagsilvadev.erpapi.financial.internal.repository.*;
import com.thiagsilvadev.erpapi.financial.internal.service.AccountingPostingService;
import com.thiagsilvadev.erpapi.financial.internal.service.ChartOfAccountsService;
import com.thiagsilvadev.erpapi.sales.api.PaymentMethod;
import com.thiagsilvadev.erpapi.sales.api.SalesFacade;
import com.thiagsilvadev.erpapi.sales.api.SalesOrderSnapshot;

@Component
@Profile("dev")
@Order(700)
class FinancialDevSeeder implements ApplicationRunner {

    private static final String CUSTOMER_DOCUMENT = "12345678901";
    private static final String ACME_CNPJ = "97.986.234/0001-91";

    private static final List<InvoiceSeed> INVOICES = List.of(
            new InvoiceSeed(58, "900.00", "BRL", -50, InvoiceSeedStatus.PAID, "900.00", 56),
            new InvoiceSeed(52, "650.00", "BRL", -10, InvoiceSeedStatus.OPEN, null, null),
            new InvoiceSeed(45, "1200.00", "BRL", -30, InvoiceSeedStatus.CANCELLED, null, null),
            new InvoiceSeed(38, "500.00", "BRL", -25, InvoiceSeedStatus.PAID, "500.00", 36),
            new InvoiceSeed(34, "250.00", "USD", -20, InvoiceSeedStatus.PAID, "250.00", 32),
            new InvoiceSeed(28, "1500.00", "BRL", -15, InvoiceSeedStatus.PAID, "1500.00", 26),
            new InvoiceSeed(23, "800.00", "BRL", 10, InvoiceSeedStatus.OPEN, null, null),
            new InvoiceSeed(18, "1100.00", "BRL", -2, InvoiceSeedStatus.PARTIALLY_PAID, "400.00", 12),
            new InvoiceSeed(13, "700.00", "BRL", 5, InvoiceSeedStatus.CANCELLED, null, null),
            new InvoiceSeed(8, "950.00", "BRL", -1, InvoiceSeedStatus.PAID, "950.00", 5),
            new InvoiceSeed(4, "400.00", "BRL", -1, InvoiceSeedStatus.OPEN, null, null),
            new InvoiceSeed(2, "300.00", "USD", 20, InvoiceSeedStatus.OPEN, null, null));

    private final InvoiceRepository invoiceRepository;
    private final FinancialObligationRepository financialObligationRepository;
    private final FinancialAccountRepository accountRepository;
    private final PaymentRepository paymentRepository;
    private final CashMovementRepository entryRepository;
    private final AccountingEntryRepository accountingEntryRepository;
    private final CompanyFacade companyFacade;
    private final CustomerFacade customerFacade;
    private final BusinessNumberService businessNumberService;
    private final ChartOfAccountsService chartOfAccountsService;
    private final AccountingPostingService accountingPostingService;
    private final SalesFacade salesFacade;
    private final Clock clock;

    FinancialDevSeeder(
            InvoiceRepository invoiceRepository,
            FinancialObligationRepository financialObligationRepository,
            FinancialAccountRepository accountRepository,
            PaymentRepository paymentRepository,
            CashMovementRepository entryRepository,
            AccountingEntryRepository accountingEntryRepository,
            CompanyFacade companyFacade,
            CustomerFacade customerFacade,
            BusinessNumberService businessNumberService,
            ChartOfAccountsService chartOfAccountsService,
            AccountingPostingService accountingPostingService,
            SalesFacade salesFacade,
            Clock clock) {
        this.invoiceRepository = invoiceRepository;
        this.financialObligationRepository = financialObligationRepository;
        this.accountRepository = accountRepository;
        this.paymentRepository = paymentRepository;
        this.entryRepository = entryRepository;
        this.accountingEntryRepository = accountingEntryRepository;
        this.companyFacade = companyFacade;
        this.customerFacade = customerFacade;
        this.businessNumberService = businessNumberService;
        this.chartOfAccountsService = chartOfAccountsService;
        this.accountingPostingService = accountingPostingService;
        this.salesFacade = salesFacade;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        UUID companyId = companyFacade.resolveCompany(ACME_CNPJ).id();
        chartOfAccountsService.ensureDefaultAccounts(companyId);
        LocalDate today = LocalDate.now(clock);
        seedManualAccountingEntries(companyId, today);
        FinancialAccount destinationAccount = ensureAccounts(companyId);
        if (invoiceRepository.countByCompanyId(companyId) >= INVOICES.size()) {
            return;
        }
        var customer = customerFacade.resolveCustomer(companyId, CUSTOMER_DOCUMENT);
        List<SalesOrderSnapshot> seededOrders = salesFacade.listOrders(companyId);

        int seedIndex = 0;
        for (InvoiceSeed seed : INVOICES) {
            UUID salesOrderReference = seedIndex < seededOrders.size()
                    ? seededOrders.get(seedIndex).id()
                    : null;
            seedIndex++;
            Invoice invoice = invoiceRepository.saveAndFlush(Invoice.create(
                    businessNumberService.next(BusinessNumberType.INVOICE),
                    companyId,
                    BillingPartyType.CUSTOMER,
                    customer.id(),
                    salesOrderReference,
                    seed.amount(),
                    seed.currencyCode(),
                    today.plusDays(seed.dueDateOffsetDays())));
            invoiceRepository.updateCreatedAtForDevSeed(
                    invoice.getId(),
                    today.minusDays(seed.daysAgo()).atTime(12, 0).toInstant(ZoneOffset.UTC));
            FinancialObligation financialObligation = financialObligationRepository.save(FinancialObligation.receivable(
                    companyId, invoice.getId(), invoice.getAmount(), invoice.getCurrencyCode(), invoice.getDueDate()));

            switch (seed.status()) {
                case OPEN -> {
                    accountingPostingService.postInvoiceRevenue(invoice);
                    backdateInvoiceAccountingEntry(invoice, today.minusDays(seed.daysAgo()));
                }
                case CANCELLED -> {
                    invoice.cancel();
                    financialObligation.cancel();
                }
                case PAID, PARTIALLY_PAID -> {
                    accountingPostingService.postInvoiceRevenue(invoice);
                    backdateInvoiceAccountingEntry(invoice, today.minusDays(seed.daysAgo()));
                    seedPayment(
                            companyId,
                            customer.id(),
                            destinationAccount,
                            invoice,
                            financialObligation,
                            salesOrderReference,
                            seed,
                            today);
                }
            }
        }
    }

    private void backdateInvoiceAccountingEntry(Invoice invoice, LocalDate competenceDate) {
        accountingEntryRepository
                .findByCompanyIdAndSourceTypeAndSourceIdAndStatus(
                        invoice.getCompanyId(),
                        AccountingEntrySourceType.INVOICE,
                        invoice.getId(),
                        AccountingEntryStatus.POSTED)
                .forEach(entry ->
                        accountingEntryRepository.updateCompetenceDateForDevSeed(entry.getId(), competenceDate));
    }

    private void seedManualAccountingEntries(UUID companyId, LocalDate today) {
        if (accountingEntryRepository.countByCompanyIdAndSourceType(companyId, AccountingEntrySourceType.MANUAL) > 0) {
            return;
        }
        AccountingAccount adminExpense =
                chartOfAccountsService.requireBySystemRole(companyId, AccountingSystemRole.ADMIN_EXPENSE);
        AccountingAccount cogs = chartOfAccountsService.requireBySystemRole(companyId, AccountingSystemRole.COGS);
        AccountingEntry expense = accountingEntryRepository.saveAndFlush(AccountingEntry.create(
                companyId,
                adminExpense.getId(),
                today.minusDays(20),
                new BigDecimal("350.00"),
                "BRL",
                AccountingEntryDirection.DEBIT,
                AccountingEntrySourceType.MANUAL,
                null,
                "Development seed admin expense"));
        accountingEntryRepository.updateCompetenceDateForDevSeed(expense.getId(), today.minusDays(20));
        AccountingEntry cogsEntry = accountingEntryRepository.saveAndFlush(AccountingEntry.create(
                companyId,
                cogs.getId(),
                today.minusDays(15),
                new BigDecimal("1200.00"),
                "BRL",
                AccountingEntryDirection.DEBIT,
                AccountingEntrySourceType.MANUAL,
                null,
                "Development seed COGS"));
        accountingEntryRepository.updateCompetenceDateForDevSeed(cogsEntry.getId(), today.minusDays(15));
    }

    private FinancialAccount ensureAccounts(UUID companyId) {
        var accounts = accountRepository
                .findAllByCompanyId(companyId, Pageable.unpaged())
                .getContent();
        if (!accounts.isEmpty()) {
            return accounts.getFirst();
        }
        FinancialAccount cash = accountRepository.saveAndFlush(FinancialAccount.create(
                companyId, "Caixa Loja Principal", FinancialAccountType.CASH_REGISTER, "Caixa físico da loja"));
        accountRepository.save(FinancialAccount.create(
                companyId, "Itaú PJ", FinancialAccountType.BANK_ACCOUNT, "Conta corrente principal"));
        return cash;
    }

    private void seedPayment(
            UUID companyId,
            UUID customerId,
            FinancialAccount destinationAccount,
            Invoice invoice,
            FinancialObligation financialObligation,
            UUID salesOrderReference,
            InvoiceSeed seed,
            LocalDate today) {
        LocalDate paidAt = today.minusDays(seed.paymentDaysAgo());
        Payment payment = Payment.create(
                businessNumberService.next(BusinessNumberType.PAYMENT),
                companyId,
                salesOrderReference,
                customerId,
                invoice.getId(),
                destinationAccount.getId(),
                seed.paidAmount(),
                PaymentMethod.PIX,
                PaymentStatus.PAID,
                invoice.getDueDate());
        payment.markAsPaid(paidAt);
        payment = paymentRepository.saveAndFlush(payment);

        CashMovement entry = entryRepository.saveAndFlush(CashMovement.income(
                companyId,
                destinationAccount.getId(),
                CashMovementSource.PAYMENT,
                seed.paidAmount(),
                seed.currencyCode(),
                payment.getId(),
                "Development seed payment"));
        entryRepository.updateOccurredAtForDevSeed(
                entry.getId(), paidAt.atTime(12, 0).toInstant(ZoneOffset.UTC));

        if (seed.status() == InvoiceSeedStatus.PAID) {
            invoice.markPaid();
            financialObligation.settle();
        }
    }

    static int seedInvoiceCount() {
        return INVOICES.size();
    }

    private enum InvoiceSeedStatus {
        OPEN,
        PARTIALLY_PAID,
        PAID,
        CANCELLED
    }

    private record InvoiceSeed(
            int daysAgo,
            BigDecimal amount,
            String currencyCode,
            int dueDateOffsetDays,
            InvoiceSeedStatus status,
            BigDecimal paidAmount,
            Integer paymentDaysAgo) {

        private InvoiceSeed(
                int daysAgo,
                String amount,
                String currencyCode,
                int dueDateOffsetDays,
                InvoiceSeedStatus status,
                String paidAmount,
                Integer paymentDaysAgo) {
            this(
                    daysAgo,
                    new BigDecimal(amount),
                    currencyCode,
                    dueDateOffsetDays,
                    status,
                    paidAmount == null ? null : new BigDecimal(paidAmount),
                    paymentDaysAgo);
        }
    }
}
