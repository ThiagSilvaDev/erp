package com.thiagsilvadev.erpapi.financial.internal.usecase.invoice;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ConflictException;
import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.domain.numbering.BusinessNumberService;
import com.thiagsilvadev.erpapi.common.domain.numbering.BusinessNumberType;
import com.thiagsilvadev.erpapi.company.api.CompanyFacade;
import com.thiagsilvadev.erpapi.customer.api.CustomerFacade;
import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialMapper;
import com.thiagsilvadev.erpapi.financial.internal.dto.invoice.CreateInvoiceRequest;
import com.thiagsilvadev.erpapi.financial.internal.dto.invoice.InvoiceResponse;
import com.thiagsilvadev.erpapi.financial.internal.model.BillingPartyType;
import com.thiagsilvadev.erpapi.financial.internal.model.Invoice;
import com.thiagsilvadev.erpapi.financial.internal.model.LedgerEntry;
import com.thiagsilvadev.erpapi.financial.internal.repository.InvoiceRepository;
import com.thiagsilvadev.erpapi.financial.internal.repository.LedgerEntryRepository;
import com.thiagsilvadev.erpapi.sales.api.OrderStatus;
import com.thiagsilvadev.erpapi.sales.api.SalesFacade;
import com.thiagsilvadev.erpapi.sales.api.SalesOrderSnapshot;

@Component
public class CreateInvoiceUseCase {

    private final InvoiceRepository invoiceRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final CustomerFacade customerFacade;
    private final CompanyFacade companyFacade;
    private final SalesFacade salesFacade;
    private final FinancialMapper mapper;
    private final BusinessNumberService businessNumberService;

    public CreateInvoiceUseCase(
            InvoiceRepository invoiceRepository,
            LedgerEntryRepository ledgerEntryRepository,
            CustomerFacade customerFacade,
            CompanyFacade companyFacade,
            SalesFacade salesFacade,
            FinancialMapper mapper,
            BusinessNumberService businessNumberService) {
        this.invoiceRepository = invoiceRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.customerFacade = customerFacade;
        this.companyFacade = companyFacade;
        this.salesFacade = salesFacade;
        this.mapper = mapper;
        this.businessNumberService = businessNumberService;
    }

    @Transactional
    @PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.financial.api.FinancialPermissions).WRITE)")
    public InvoiceResponse execute(CreateInvoiceRequest request) {
        validateBillingParty(request.billingPartyType(), request.billingPartyId());

        if (request.salesOrderId() != null) {
            SalesOrderSnapshot order = salesFacade
                    .findOrderById(request.salesOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("SalesOrder", request.salesOrderId()));
            validateSalesOrderInvoice(request, order);
        }

        Invoice invoice = invoiceRepository.save(Invoice.create(
                businessNumberService.next(BusinessNumberType.INVOICE),
                request.billingPartyType(),
                request.billingPartyId(),
                request.salesOrderId(),
                request.amount(),
                request.currencyCode(),
                request.dueDate()));

        ledgerEntryRepository.save(LedgerEntry.receivable(
                invoice.getId(), invoice.getAmount(), invoice.getCurrencyCode(), invoice.getDueDate()));

        return mapper.toResponse(invoice);
    }

    private void validateSalesOrderInvoice(CreateInvoiceRequest request, SalesOrderSnapshot order) {
        if (order.status() != OrderStatus.CONFIRMED) {
            throw new ConflictException("Only confirmed sales orders can be invoiced.");
        }
        BillingPartyType expectedType = order.saleType() == com.thiagsilvadev.erpapi.sales.api.SaleType.B2B
                ? BillingPartyType.COMPANY
                : BillingPartyType.CUSTOMER;
        UUID expectedId = order.saleType() == com.thiagsilvadev.erpapi.sales.api.SaleType.B2B
                ? order.buyerCompanyId()
                : order.customerId();
        if (request.billingPartyType() != expectedType || !expectedId.equals(request.billingPartyId())) {
            throw new ConflictException("Invoice billing party must match the sales order billing party.");
        }
        if (order.totalAmount().compareTo(request.amount()) != 0) {
            throw new ConflictException("Invoice amount must match the sales order total.");
        }
        if (!order.currencyCode().equalsIgnoreCase(request.currencyCode())) {
            throw new ConflictException("Invoice currency must match the sales order currency.");
        }
        if (invoiceRepository.existsBySalesOrderId(order.id())) {
            throw new ConflictException("Sales order already has an invoice.");
        }
    }

    private void validateBillingParty(BillingPartyType billingPartyType, UUID billingPartyId) {
        if (billingPartyType == BillingPartyType.CUSTOMER) {
            customerFacade
                    .findCustomerById(billingPartyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer", billingPartyId));
            return;
        }

        companyFacade
                .findById(billingPartyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", billingPartyId));
    }
}
