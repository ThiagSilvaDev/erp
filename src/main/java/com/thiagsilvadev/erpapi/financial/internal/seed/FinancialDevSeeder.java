package com.thiagsilvadev.erpapi.financial.internal.seed;

import java.time.LocalDate;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.numbering.BusinessNumberService;
import com.thiagsilvadev.erpapi.common.domain.numbering.BusinessNumberType;
import com.thiagsilvadev.erpapi.company.api.CompanyFacade;
import com.thiagsilvadev.erpapi.customer.api.CustomerFacade;
import com.thiagsilvadev.erpapi.financial.internal.model.BillingPartyType;
import com.thiagsilvadev.erpapi.financial.internal.model.FinancialAccount;
import com.thiagsilvadev.erpapi.financial.internal.model.FinancialAccountType;
import com.thiagsilvadev.erpapi.financial.internal.model.Invoice;
import com.thiagsilvadev.erpapi.financial.internal.model.LedgerEntry;
import com.thiagsilvadev.erpapi.financial.internal.repository.FinancialAccountRepository;
import com.thiagsilvadev.erpapi.financial.internal.repository.InvoiceRepository;
import com.thiagsilvadev.erpapi.financial.internal.repository.LedgerEntryRepository;

@Component
@Profile("dev")
@Order(700)
class FinancialDevSeeder implements ApplicationRunner {

    private static final String CUSTOMER_DOCUMENT = "12345678901";
    private static final String ACME_CNPJ = "97.986.234/0001-91";

    private final InvoiceRepository invoiceRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final FinancialAccountRepository accountRepository;
    private final CompanyFacade companyFacade;
    private final CustomerFacade customerFacade;
    private final BusinessNumberService businessNumberService;

    FinancialDevSeeder(
            InvoiceRepository invoiceRepository,
            LedgerEntryRepository ledgerEntryRepository,
            FinancialAccountRepository accountRepository,
            CompanyFacade companyFacade,
            CustomerFacade customerFacade,
            BusinessNumberService businessNumberService) {
        this.invoiceRepository = invoiceRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.accountRepository = accountRepository;
        this.companyFacade = companyFacade;
        this.customerFacade = customerFacade;
        this.businessNumberService = businessNumberService;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        var companyId = companyFacade
                .findByCnpj(ACME_CNPJ)
                .orElseThrow(() -> new IllegalStateException("Seed company must exist before seeding finance"))
                .id();

        if (accountRepository
                .findAllByCompanyId(companyId, org.springframework.data.domain.Pageable.unpaged())
                .isEmpty()) {
            accountRepository.save(FinancialAccount.create(
                    companyId, "Caixa Loja Principal", FinancialAccountType.CASH_REGISTER, "Caixa físico da loja"));
            accountRepository.save(FinancialAccount.create(
                    companyId, "Itaú PJ", FinancialAccountType.BANK_ACCOUNT, "Conta corrente principal"));
        }

        if (!invoiceRepository
                .findAllByCompanyId(companyId, org.springframework.data.domain.Pageable.unpaged())
                .isEmpty()) {
            return;
        }
        var customer = customerFacade
                .findCustomerByDocument(CUSTOMER_DOCUMENT)
                .orElseThrow(() -> new IllegalStateException("Seed customer must exist before seeding finance"));
        Invoice invoice = invoiceRepository.save(Invoice.create(
                businessNumberService.next(BusinessNumberType.INVOICE),
                companyId,
                BillingPartyType.CUSTOMER,
                customer.id(),
                null,
                new java.math.BigDecimal("21.00"),
                "BRL",
                LocalDate.now().plusDays(15)));
        ledgerEntryRepository.save(LedgerEntry.receivable(
                companyId, invoice.getId(), invoice.getAmount(), invoice.getCurrencyCode(), invoice.getDueDate()));
    }
}
