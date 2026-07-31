package com.thiagsilvadev.erpapi.financial.internal.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.thiagsilvadev.erpapi.common.domain.numbering.BusinessNumberService;
import com.thiagsilvadev.erpapi.common.domain.numbering.BusinessNumberType;
import com.thiagsilvadev.erpapi.financial.internal.model.BillingPartyType;
import com.thiagsilvadev.erpapi.financial.internal.model.Invoice;
import com.thiagsilvadev.erpapi.financial.internal.repository.InvoiceRepository;
import com.thiagsilvadev.erpapi.financial.internal.repository.LedgerEntryRepository;
import com.thiagsilvadev.erpapi.financial.internal.repository.PaymentRepository;
import com.thiagsilvadev.erpapi.sales.api.*;

@ExtendWith(MockitoExtension.class)
class SalesOrderFinancialListenerTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private LedgerEntryRepository ledgerEntryRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BusinessNumberService businessNumberService;

    @Mock
    private SalesFacade salesFacade;

    @Test
    void shouldCreateB2bInvoiceForBuyerCompany() {
        UUID salesOrderId = UUID.randomUUID();
        UUID sellerCompanyId = UUID.randomUUID();
        UUID buyerCompanyId = UUID.randomUUID();
        SalesOrderConfirmedEvent event = new SalesOrderConfirmedEvent(salesOrderId, sellerCompanyId, LocalDate.now());
        when(invoiceRepository.existsBySalesOrderId(event.salesOrderId())).thenReturn(false);
        when(salesFacade.getOrder(sellerCompanyId, salesOrderId))
                .thenReturn(order(salesOrderId, sellerCompanyId, SaleType.B2B, buyerCompanyId, null));
        when(businessNumberService.next(BusinessNumberType.INVOICE)).thenReturn(300L);
        when(invoiceRepository.save(org.mockito.ArgumentMatchers.any(Invoice.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        listener().on(event);

        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepository).save(invoiceCaptor.capture());
        assertThat(invoiceCaptor.getValue().getBillingPartyType()).isEqualTo(BillingPartyType.COMPANY);
        assertThat(invoiceCaptor.getValue().getBillingPartyId()).isEqualTo(buyerCompanyId);
    }

    @Test
    void shouldCreateB2cInvoiceForCustomer() {
        UUID salesOrderId = UUID.randomUUID();
        UUID sellerCompanyId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        SalesOrderConfirmedEvent event = new SalesOrderConfirmedEvent(salesOrderId, sellerCompanyId, LocalDate.now());
        when(invoiceRepository.existsBySalesOrderId(event.salesOrderId())).thenReturn(false);
        when(salesFacade.getOrder(sellerCompanyId, salesOrderId))
                .thenReturn(order(salesOrderId, sellerCompanyId, SaleType.B2C, null, customerId));
        when(businessNumberService.next(BusinessNumberType.INVOICE)).thenReturn(301L);
        when(invoiceRepository.save(org.mockito.ArgumentMatchers.any(Invoice.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        listener().on(event);

        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepository).save(invoiceCaptor.capture());
        assertThat(invoiceCaptor.getValue().getBillingPartyType()).isEqualTo(BillingPartyType.CUSTOMER);
        assertThat(invoiceCaptor.getValue().getBillingPartyId()).isEqualTo(customerId);
    }

    private SalesOrderFinancialListener listener() {
        return new SalesOrderFinancialListener(
                invoiceRepository, ledgerEntryRepository, paymentRepository, businessNumberService, salesFacade);
    }

    private static SalesOrderSnapshot order(
            UUID id, UUID sellerCompanyId, SaleType saleType, UUID buyerCompanyId, UUID customerId) {
        return new SalesOrderSnapshot(
                id,
                100L,
                saleType,
                FulfillmentType.PICKUP,
                PaymentMethod.PIX,
                null,
                sellerCompanyId,
                buyerCompanyId,
                customerId,
                OrderStatus.CONFIRMED,
                new BigDecimal("20.00"),
                "BRL",
                Set.of());
    }
}
