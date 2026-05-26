package com.thiagsilvadev.erpapi.sales.internal.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.thiagsilvadev.erpapi.common.domain.model.Money;
import com.thiagsilvadev.erpapi.company.api.CompanyContactSnapshot;
import com.thiagsilvadev.erpapi.company.api.CompanyFacade;
import com.thiagsilvadev.erpapi.company.api.CompanySnapshot;
import com.thiagsilvadev.erpapi.company.api.CompanyStatus;
import com.thiagsilvadev.erpapi.company.api.CompanyType;
import com.thiagsilvadev.erpapi.customer.api.CustomerFacade;
import com.thiagsilvadev.erpapi.customer.api.CustomerSnapshot;
import com.thiagsilvadev.erpapi.sales.api.FulfillmentType;
import com.thiagsilvadev.erpapi.sales.api.PaymentMethod;
import com.thiagsilvadev.erpapi.sales.internal.dto.SalesMapperImpl;
import com.thiagsilvadev.erpapi.sales.internal.model.SalesOrder;
import com.thiagsilvadev.erpapi.sales.internal.model.SalesOrderItem;

@ExtendWith(MockitoExtension.class)
class SalesOrderResponseAssemblerTest {

    @Mock
    private CustomerFacade customerFacade;

    @Mock
    private CompanyFacade companyFacade;

    @Test
    void shouldAddCustomerBuyerSummaryForB2cOrder() {
        UUID sellerCompanyId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        SalesOrder order = SalesOrder.createB2C(
                100L, FulfillmentType.PICKUP, PaymentMethod.PIX, null, sellerCompanyId, customerId, Set.of(item()));
        when(companyFacade.findById(sellerCompanyId))
                .thenReturn(Optional.of(
                        company(sellerCompanyId, CompanyType.CORRESPONDENT, "Seller Legal", "Seller Trade")));
        when(customerFacade.findCustomerById(customerId))
                .thenReturn(Optional.of(new CustomerSnapshot(customerId, "Customer Name", "123", UUID.randomUUID())));

        var response = assembler().toResponse(order);

        assertThat(response.sellerSummary().companyName()).isEqualTo("Seller Trade");
        assertThat(response.buyerSummary().buyerName()).isEqualTo("Customer Name");
        assertThat(response.buyerSummary().companyName()).isNull();
        assertThat(response.formattedTotalAmount()).isEqualTo("BRL 20.00");
    }

    @Test
    void shouldAddContactAndCompanyBuyerSummaryForB2bOrder() {
        UUID sellerCompanyId = UUID.randomUUID();
        UUID buyerCompanyId = UUID.randomUUID();
        SalesOrder order = SalesOrder.createB2B(
                100L, FulfillmentType.PICKUP, PaymentMethod.PIX, null, sellerCompanyId, buyerCompanyId, Set.of(item()));
        var primaryContact = new CompanyContactSnapshot(
                UUID.randomUUID(), buyerCompanyId, "Buyer Contact", "Purchasing", null, true);
        when(companyFacade.findById(sellerCompanyId))
                .thenReturn(Optional.of(company(sellerCompanyId, CompanyType.FACTORY, "Seller Legal", "Seller Trade")));
        when(companyFacade.findById(buyerCompanyId))
                .thenReturn(Optional.of(new CompanySnapshot(
                        buyerCompanyId,
                        1L,
                        "Legal Company",
                        "Trade Company",
                        null,
                        CompanyStatus.ACTIVE,
                        CompanyType.CORRESPONDENT,
                        null,
                        null,
                        primaryContact)));

        var response = assembler().toResponse(order);

        assertThat(response.sellerSummary().companyName()).isEqualTo("Seller Trade");
        assertThat(response.buyerSummary().buyerName()).isEqualTo("Buyer Contact");
        assertThat(response.buyerSummary().companyName()).isEqualTo("Trade Company");
        assertThat(response.formattedTotalAmount()).isEqualTo("BRL 20.00");
    }

    private SalesOrderResponseAssembler assembler() {
        return new SalesOrderResponseAssembler(new SalesMapperImpl(), customerFacade, companyFacade);
    }

    private static CompanySnapshot company(UUID id, CompanyType type, String legalName, String tradeName) {
        return new CompanySnapshot(id, 1L, legalName, tradeName, null, CompanyStatus.ACTIVE, type, null, null, null);
    }

    private static SalesOrderItem item() {
        return SalesOrderItem.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "SKU-001",
                "Product",
                2,
                Money.of(new BigDecimal("10.00"), "BRL"));
    }
}
