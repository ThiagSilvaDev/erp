package com.thiagsilvadev.erpapi.sales.internal.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.thiagsilvadev.erpapi.catalog.api.CatalogFacade;
import com.thiagsilvadev.erpapi.catalog.api.ProductSnapshot;
import com.thiagsilvadev.erpapi.catalog.api.ProductStatus;
import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.domain.model.Money;
import com.thiagsilvadev.erpapi.common.domain.numbering.BusinessNumberService;
import com.thiagsilvadev.erpapi.common.domain.numbering.BusinessNumberType;
import com.thiagsilvadev.erpapi.company.api.CompanyFacade;
import com.thiagsilvadev.erpapi.company.api.CompanySnapshot;
import com.thiagsilvadev.erpapi.company.api.CompanyStatus;
import com.thiagsilvadev.erpapi.company.api.CompanyType;
import com.thiagsilvadev.erpapi.customer.api.CustomerFacade;
import com.thiagsilvadev.erpapi.customer.api.CustomerSnapshot;
import com.thiagsilvadev.erpapi.sales.api.FulfillmentType;
import com.thiagsilvadev.erpapi.sales.api.PaymentMethod;
import com.thiagsilvadev.erpapi.sales.api.SaleType;
import com.thiagsilvadev.erpapi.sales.internal.dto.CreateSalesOrderItemRequest;
import com.thiagsilvadev.erpapi.sales.internal.dto.CreateSalesOrderRequest;
import com.thiagsilvadev.erpapi.sales.internal.dto.SalesMapper;
import com.thiagsilvadev.erpapi.sales.internal.model.SalesOrder;
import com.thiagsilvadev.erpapi.sales.internal.repository.SalesOrderRepository;

@ExtendWith(MockitoExtension.class)
class CreateSalesOrderUseCaseTest {

    @Mock
    private SalesOrderRepository orderRepository;

    @Mock
    private CustomerFacade customerFacade;

    @Mock
    private CompanyFacade companyFacade;

    @Mock
    private CatalogFacade catalogFacade;

    @Mock
    private SalesMapper mapper;

    @Mock
    private BusinessNumberService businessNumberService;

    @Test
    void shouldCreateB2bOrder() {
        UUID sellerCompanyId = UUID.randomUUID();
        UUID buyerCompanyId = UUID.randomUUID();
        CreateSalesOrderRequest request = request(SaleType.B2B, sellerCompanyId, buyerCompanyId, null);
        givenCompany(sellerCompanyId, CompanyType.FACTORY);
        givenCompany(buyerCompanyId, CompanyType.CORRESPONDENT);
        givenProduct(request.items().getFirst().productId());
        when(businessNumberService.next(BusinessNumberType.SALES_ORDER)).thenReturn(100L);
        when(orderRepository.save(org.mockito.ArgumentMatchers.any(SalesOrder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        useCase().execute(request);

        ArgumentCaptor<SalesOrder> orderCaptor = ArgumentCaptor.forClass(SalesOrder.class);
        verify(orderRepository).save(orderCaptor.capture());
        SalesOrder order = orderCaptor.getValue();
        assertThat(order.getSaleType()).isEqualTo(SaleType.B2B);
        assertThat(order.getSellerCompanyId()).isEqualTo(sellerCompanyId);
        assertThat(order.getBuyerCompanyId()).isEqualTo(buyerCompanyId);
        assertThat(order.getCustomerId()).isNull();
    }

    @Test
    void shouldCreateB2cOrder() {
        UUID sellerCompanyId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        CreateSalesOrderRequest request = request(SaleType.B2C, sellerCompanyId, null, customerId);
        givenCompany(sellerCompanyId, CompanyType.CORRESPONDENT);
        when(customerFacade.findCustomerById(customerId))
                .thenReturn(Optional.of(new CustomerSnapshot(customerId, "Customer", "123", sellerCompanyId)));
        givenProduct(request.items().getFirst().productId());
        when(businessNumberService.next(BusinessNumberType.SALES_ORDER)).thenReturn(101L);
        when(orderRepository.save(org.mockito.ArgumentMatchers.any(SalesOrder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        useCase().execute(request);

        ArgumentCaptor<SalesOrder> orderCaptor = ArgumentCaptor.forClass(SalesOrder.class);
        verify(orderRepository).save(orderCaptor.capture());
        SalesOrder order = orderCaptor.getValue();
        assertThat(order.getSaleType()).isEqualTo(SaleType.B2C);
        assertThat(order.getSellerCompanyId()).isEqualTo(sellerCompanyId);
        assertThat(order.getBuyerCompanyId()).isNull();
        assertThat(order.getCustomerId()).isEqualTo(customerId);
    }

    @Test
    void shouldRejectB2bWhenBuyerEqualsSeller() {
        UUID companyId = UUID.randomUUID();
        CreateSalesOrderRequest request = request(SaleType.B2B, companyId, companyId, null);
        givenCompany(companyId, CompanyType.FACTORY);

        assertThatThrownBy(() -> useCase().execute(request)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectB2cCustomerFromAnotherOwnerCompany() {
        UUID sellerCompanyId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        CreateSalesOrderRequest request = request(SaleType.B2C, sellerCompanyId, null, customerId);
        givenCompany(sellerCompanyId, CompanyType.FACTORY);
        when(customerFacade.findCustomerById(customerId))
                .thenReturn(Optional.of(new CustomerSnapshot(customerId, "Customer", "123", UUID.randomUUID())));

        assertThatThrownBy(() -> useCase().execute(request)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldReportMissingProductResourceName() {
        UUID sellerCompanyId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        CreateSalesOrderRequest request = request(SaleType.B2C, sellerCompanyId, null, customerId);
        givenCompany(sellerCompanyId, CompanyType.FACTORY);
        when(customerFacade.findCustomerById(customerId))
                .thenReturn(Optional.of(new CustomerSnapshot(customerId, "Customer", "123", sellerCompanyId)));
        when(catalogFacade.findAllProductsByIdIn(
                        java.util.Set.of(request.items().getFirst().productId())))
                .thenReturn(List.of());

        assertThatThrownBy(() -> useCase().execute(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .satisfies(exception -> assertThat(((ResourceNotFoundException) exception).getProperties())
                        .containsEntry("resourceName", "Product"));
    }

    @Test
    void shouldReportMissingCustomerResourceName() {
        UUID sellerCompanyId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        CreateSalesOrderRequest request = request(SaleType.B2C, sellerCompanyId, null, customerId);
        givenCompany(sellerCompanyId, CompanyType.FACTORY);
        when(customerFacade.findCustomerById(customerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase().execute(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .satisfies(exception -> assertThat(((ResourceNotFoundException) exception).getProperties())
                        .containsEntry("resourceName", "Customer"));
    }

    private CreateSalesOrderUseCase useCase() {
        return new CreateSalesOrderUseCase(
                orderRepository, customerFacade, companyFacade, catalogFacade, mapper, businessNumberService);
    }

    private void givenCompany(UUID id, CompanyType type) {
        when(companyFacade.findById(id))
                .thenReturn(Optional.of(new CompanySnapshot(
                        id, 1L, "Company", "Company", null, CompanyStatus.ACTIVE, type, null, null, null)));
    }

    private void givenProduct(UUID productId) {
        when(catalogFacade.findAllProductsByIdIn(java.util.Set.of(productId)))
                .thenReturn(List.of(new ProductSnapshot(
                        productId,
                        "SKU-001",
                        "Product",
                        "Product",
                        Money.of(new BigDecimal("10.00"), "BRL"),
                        ProductStatus.ACTIVE,
                        null)));
    }

    private static CreateSalesOrderRequest request(
            SaleType saleType, UUID sellerCompanyId, UUID buyerCompanyId, UUID customerId) {
        return new CreateSalesOrderRequest(
                saleType,
                FulfillmentType.PICKUP,
                null,
                PaymentMethod.PIX,
                sellerCompanyId,
                buyerCompanyId,
                customerId,
                List.of(new CreateSalesOrderItemRequest(UUID.randomUUID(), UUID.randomUUID(), 2)));
    }
}
