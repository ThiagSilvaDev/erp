package com.thiagsilvadev.erpapi.financial.internal.usecase.invoice;

import static com.thiagsilvadev.erpapi.financial.api.FinancialPermissions.WRITE;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ConflictException;
import com.thiagsilvadev.erpapi.common.domain.numbering.BusinessNumberService;
import com.thiagsilvadev.erpapi.common.domain.numbering.BusinessNumberType;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.company.api.CompanyFacade;
import com.thiagsilvadev.erpapi.customer.api.CustomerFacade;
import com.thiagsilvadev.erpapi.financial.internal.dto.invoice.CreateInvoiceRequest;
import com.thiagsilvadev.erpapi.financial.internal.dto.invoice.InvoiceResponse;
import com.thiagsilvadev.erpapi.financial.internal.mapper.FinancialMapper;
import com.thiagsilvadev.erpapi.financial.internal.model.BillingPartyType;
import com.thiagsilvadev.erpapi.financial.internal.model.FinancialObligation;
import com.thiagsilvadev.erpapi.financial.internal.model.Invoice;
import com.thiagsilvadev.erpapi.financial.internal.repository.FinancialObligationRepository;
import com.thiagsilvadev.erpapi.financial.internal.repository.InvoiceRepository;
import com.thiagsilvadev.erpapi.financial.internal.service.AccountingPostingService;
import com.thiagsilvadev.erpapi.sales.api.SaleType;
import com.thiagsilvadev.erpapi.sales.api.SalesFacade;
import com.thiagsilvadev.erpapi.sales.api.SalesOrderSnapshot;
import com.thiagsilvadev.erpapi.sales.api.SalesOrderStatus;

@Component
public class CreateInvoiceUseCase {

    private final InvoiceRepository invoiceRepository;
    private final FinancialObligationRepository financialObligationRepository;
    private final CustomerFacade customerFacade;
    private final CompanyFacade companyFacade;
    private final SalesFacade salesFacade;
    private final FinancialMapper mapper;
    private final BusinessNumberService businessNumberService;
    private final AccountingPostingService accountingPostingService;

    public CreateInvoiceUseCase(
            InvoiceRepository invoiceRepository,
            FinancialObligationRepository financialObligationRepository,
            CustomerFacade customerFacade,
            CompanyFacade companyFacade,
            SalesFacade salesFacade,
            FinancialMapper mapper,
            BusinessNumberService businessNumberService,
            AccountingPostingService accountingPostingService) {
        this.invoiceRepository = invoiceRepository;
        this.financialObligationRepository = financialObligationRepository;
        this.customerFacade = customerFacade;
        this.companyFacade = companyFacade;
        this.salesFacade = salesFacade;
        this.mapper = mapper;
        this.businessNumberService = businessNumberService;
        this.accountingPostingService = accountingPostingService;
    }

    @Transactional
    @PreAuthorize("hasAuthority('" + WRITE + "')")
    public InvoiceResponse execute(CreateInvoiceRequest request) {
        var companyId = TenantContext.requireTenantId();
        validateBillingParty(request.billingPartyType(), request.billingPartyId());

        if (request.salesOrderId() != null) {
            SalesOrderSnapshot order = salesFacade.getOrder(companyId, request.salesOrderId());
            validateSalesOrderInvoice(request, order);
        }

        Invoice invoice = invoiceRepository.save(Invoice.create(
                businessNumberService.next(BusinessNumberType.INVOICE),
                companyId,
                request.billingPartyType(),
                request.billingPartyId(),
                request.salesOrderId(),
                request.amount(),
                request.currencyCode(),
                request.dueDate()));

        financialObligationRepository.save(FinancialObligation.receivable(
                companyId, invoice.getId(), invoice.getAmount(), invoice.getCurrencyCode(), invoice.getDueDate()));
        accountingPostingService.postInvoiceRevenue(invoice);

        return mapper.toResponse(invoice);
    }

    private void validateSalesOrderInvoice(CreateInvoiceRequest request, SalesOrderSnapshot order) {
        if (order.status() != SalesOrderStatus.CONFIRMED) {
            throw new ConflictException("Only confirmed sales orders can be invoiced.");
        }
        BillingPartyType expectedType =
                order.saleType() == SaleType.B2B ? BillingPartyType.COMPANY : BillingPartyType.CUSTOMER;
        UUID expectedId = order.saleType() == SaleType.B2B ? order.buyerCompanyId() : order.customerId();
        if (request.billingPartyType() != expectedType || !expectedId.equals(request.billingPartyId())) {
            throw new ConflictException("Invoice billing party must match the sales order billing party.");
        }
        if (order.totalAmount().compareTo(request.amount()) != 0) {
            throw new ConflictException("Invoice amount must match the sales order total.");
        }
        if (!order.currencyCode().equalsIgnoreCase(request.currencyCode())) {
            throw new ConflictException("Invoice currency must match the sales order currency.");
        }
        if (invoiceRepository.existsByCompanyIdAndSalesOrderId(order.sellerCompanyId(), order.id())) {
            throw new ConflictException("Sales order already has an invoice.");
        }
    }

    private void validateBillingParty(BillingPartyType billingPartyType, UUID billingPartyId) {
        if (billingPartyType == BillingPartyType.CUSTOMER) {
            customerFacade.getCustomer(TenantContext.requireTenantId(), billingPartyId);
            return;
        }

        companyFacade.getCompany(billingPartyId);
    }
}
