package com.thiagsilvadev.erpapi.financial.internal.usecase.invoice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.thiagsilvadev.erpapi.common.domain.exception.ConflictException;
import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.domain.numbering.BusinessNumberService;
import com.thiagsilvadev.erpapi.common.domain.numbering.BusinessNumberType;
import com.thiagsilvadev.erpapi.company.api.CompanyFacade;
import com.thiagsilvadev.erpapi.company.api.CompanySnapshot;
import com.thiagsilvadev.erpapi.company.api.CompanyStatus;
import com.thiagsilvadev.erpapi.company.api.CompanyType;
import com.thiagsilvadev.erpapi.customer.api.CustomerFacade;
import com.thiagsilvadev.erpapi.customer.api.CustomerSnapshot;
import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialDtos.CreateInvoiceRequest;
import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialMapper;
import com.thiagsilvadev.erpapi.financial.internal.model.BillingPartyType;
import com.thiagsilvadev.erpapi.financial.internal.model.Invoice;
import com.thiagsilvadev.erpapi.financial.internal.repository.InvoiceRepository;
import com.thiagsilvadev.erpapi.financial.internal.repository.LedgerEntryRepository;
import com.thiagsilvadev.erpapi.sales.api.*;
import com.thiagsilvadev.erpapi.sales.api.PaymentMethod;

@ExtendWith(MockitoExtension.class)
class CreateInvoiceUseCaseTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private LedgerEntryRepository ledgerEntryRepository;

    @Mock
    private CustomerFacade customerFacade;

    @Mock
    private CompanyFacade companyFacade;

    @Mock
    private SalesFacade salesFacade;

    @Mock
    private FinancialMapper mapper;

    @Mock
    private BusinessNumberService businessNumberService;

    @Test
    void shouldCreateManualCustomerInvoice() {
        UUID customerId = UUID.randomUUID();
        CreateInvoiceRequest request = request(BillingPartyType.CUSTOMER, customerId, null);
        when(customerFacade.findCustomerById(customerId))
                .thenReturn(Optional.of(new CustomerSnapshot(customerId, "Customer", "123", UUID.randomUUID())));
        when(businessNumberService.next(BusinessNumberType.INVOICE)).thenReturn(200L);
        when(invoiceRepository.save(org.mockito.ArgumentMatchers.any(Invoice.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        useCase().execute(request);

        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepository).save(invoiceCaptor.capture());
        assertThat(invoiceCaptor.getValue().getBillingPartyType()).isEqualTo(BillingPartyType.CUSTOMER);
        assertThat(invoiceCaptor.getValue().getBillingPartyId()).isEqualTo(customerId);
    }

    @Test
    void shouldCreateB2bSalesOrderInvoiceForBuyerCompany() {
        UUID salesOrderId = UUID.randomUUID();
        UUID buyerCompanyId = UUID.randomUUID();
        CreateInvoiceRequest request = request(BillingPartyType.COMPANY, buyerCompanyId, salesOrderId);
        when(companyFacade.findById(buyerCompanyId)).thenReturn(Optional.of(company(buyerCompanyId)));
        when(salesFacade.findOrderById(salesOrderId))
                .thenReturn(Optional.of(new SalesOrderSnapshot(
                        salesOrderId,
                        100L,
                        SaleType.B2B,
                        FulfillmentType.PICKUP,
                        PaymentMethod.PIX,
                        UUID.randomUUID(),
                        buyerCompanyId,
                        null,
                        OrderStatus.CONFIRMED,
                        new BigDecimal("20.00"),
                        "BRL")));
        when(invoiceRepository.existsBySalesOrderId(salesOrderId)).thenReturn(false);
        when(businessNumberService.next(BusinessNumberType.INVOICE)).thenReturn(201L);
        when(invoiceRepository.save(org.mockito.ArgumentMatchers.any(Invoice.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        useCase().execute(request);

        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepository).save(invoiceCaptor.capture());
        assertThat(invoiceCaptor.getValue().getBillingPartyType()).isEqualTo(BillingPartyType.COMPANY);
        assertThat(invoiceCaptor.getValue().getBillingPartyId()).isEqualTo(buyerCompanyId);
    }

    @Test
    void shouldRejectSalesOrderInvoiceWithWrongBillingParty() {
        UUID salesOrderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        CreateInvoiceRequest request = request(BillingPartyType.COMPANY, UUID.randomUUID(), salesOrderId);
        when(companyFacade.findById(request.billingPartyId()))
                .thenReturn(Optional.of(company(request.billingPartyId())));
        when(salesFacade.findOrderById(salesOrderId))
                .thenReturn(Optional.of(new SalesOrderSnapshot(
                        salesOrderId,
                        100L,
                        SaleType.B2C,
                        FulfillmentType.PICKUP,
                        PaymentMethod.PIX,
                        UUID.randomUUID(),
                        null,
                        customerId,
                        OrderStatus.CONFIRMED,
                        new BigDecimal("20.00"),
                        "BRL")));

        assertThatThrownBy(() -> useCase().execute(request)).isInstanceOf(ConflictException.class);
    }

    @Test
    void shouldReportMissingSalesOrderResourceName() {
        UUID customerId = UUID.randomUUID();
        UUID salesOrderId = UUID.randomUUID();
        CreateInvoiceRequest request = request(BillingPartyType.CUSTOMER, customerId, salesOrderId);
        when(customerFacade.findCustomerById(customerId))
                .thenReturn(Optional.of(new CustomerSnapshot(customerId, "Customer", "123", UUID.randomUUID())));
        when(salesFacade.findOrderById(salesOrderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase().execute(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .satisfies(exception -> assertThat(((ResourceNotFoundException) exception).getProperties())
                        .containsEntry("resourceName", "SalesOrder"));
    }

    @Test
    void shouldReportMissingCompanyBillingPartyResourceName() {
        UUID companyId = UUID.randomUUID();
        CreateInvoiceRequest request = request(BillingPartyType.COMPANY, companyId, null);
        when(companyFacade.findById(companyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase().execute(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .satisfies(exception -> assertThat(((ResourceNotFoundException) exception).getProperties())
                        .containsEntry("resourceName", "Company"));
    }

    private CreateInvoiceUseCase useCase() {
        return new CreateInvoiceUseCase(
                invoiceRepository,
                ledgerEntryRepository,
                customerFacade,
                companyFacade,
                salesFacade,
                mapper,
                businessNumberService);
    }

    private static CreateInvoiceRequest request(
            BillingPartyType billingPartyType, UUID billingPartyId, UUID salesOrderId) {
        return new CreateInvoiceRequest(
                billingPartyType, billingPartyId, salesOrderId, new BigDecimal("20.00"), "BRL", LocalDate.now());
    }

    private static CompanySnapshot company(UUID id) {
        return new CompanySnapshot(
                id, 1L, "Company", "Company", null, CompanyStatus.ACTIVE, CompanyType.CORRESPONDENT, null, null, null);
    }
}
