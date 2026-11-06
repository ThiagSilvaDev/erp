package com.thiagsilvadev.erpapi.sales.internal.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.domain.model.Money;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.inventory.api.InventoryFacade;
import com.thiagsilvadev.erpapi.sales.api.FulfillmentType;
import com.thiagsilvadev.erpapi.sales.api.PaymentMethod;
import com.thiagsilvadev.erpapi.sales.internal.dto.SalesOrderSearchCriteria;
import com.thiagsilvadev.erpapi.sales.internal.event.SalesOrderEventPublisher;
import com.thiagsilvadev.erpapi.sales.internal.model.SalesOrder;
import com.thiagsilvadev.erpapi.sales.internal.model.SalesOrderItem;
import com.thiagsilvadev.erpapi.sales.internal.repository.SalesOrderRepository;

@ExtendWith(MockitoExtension.class)
class SalesOrderTenantScopeUseCaseTest {

    @Mock
    private SalesOrderRepository orderRepository;

    @Mock
    private SalesOrderResponseAssembler responseAssembler;

    @Mock
    private InventoryFacade inventoryFacade;

    @Mock
    private SalesOrderEventPublisher eventPublisher;

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldListOrdersUsingCurrentTenantAsSellerScope() {
        UUID tenantId = UUID.randomUUID();
        TenantContext.setTenantId(tenantId);
        var pageable = PageRequest.of(0, 20);
        when(orderRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(new PageImpl<>(List.of()));

        new SalesOrderQueryUseCase(orderRepository, responseAssembler)
                .list(new SalesOrderSearchCriteria(null, null, null, null, null), pageable);

        verify(orderRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void shouldGetOrderByIdWithinCurrentTenant() {
        UUID tenantId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        TenantContext.setTenantId(tenantId);
        when(orderRepository.findByIdAndSellerCompanyId(orderId, tenantId)).thenReturn(Optional.of(order(tenantId)));

        new SalesOrderQueryUseCase(orderRepository, responseAssembler).getById(orderId);

        verify(orderRepository).findByIdAndSellerCompanyId(orderId, tenantId);
    }

    @Test
    void shouldHideOrderByIdFromAnotherTenant() {
        UUID tenantId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        TenantContext.setTenantId(tenantId);
        when(orderRepository.findByIdAndSellerCompanyId(orderId, tenantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new SalesOrderQueryUseCase(orderRepository, responseAssembler).getById(orderId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(orderRepository, never()).findById(orderId);
    }

    @Test
    void shouldConfirmOnlyOrdersOwnedByCurrentTenant() {
        UUID tenantId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        TenantContext.setTenantId(tenantId);
        SalesOrder order = order(tenantId);
        when(orderRepository.findByIdAndSellerCompanyId(orderId, tenantId)).thenReturn(Optional.of(order));

        new ConfirmSalesOrderUseCase(orderRepository, inventoryFacade, eventPublisher, responseAssembler)
                .execute(orderId);

        verify(orderRepository).findByIdAndSellerCompanyId(orderId, tenantId);
    }

    @Test
    void shouldCancelOnlyOrdersOwnedByCurrentTenant() {
        UUID tenantId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        TenantContext.setTenantId(tenantId);
        SalesOrder order = order(tenantId);
        when(orderRepository.findByIdAndSellerCompanyId(orderId, tenantId)).thenReturn(Optional.of(order));

        new CancelSalesOrderUseCase(orderRepository, inventoryFacade, eventPublisher, responseAssembler)
                .execute(orderId);

        verify(orderRepository).findByIdAndSellerCompanyId(orderId, tenantId);
    }

    @Test
    void shouldNotReleaseReservationsWhenCancellingDraftOrder() {
        UUID tenantId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        TenantContext.setTenantId(tenantId);
        when(orderRepository.findByIdAndSellerCompanyId(orderId, tenantId)).thenReturn(Optional.of(order(tenantId)));

        new CancelSalesOrderUseCase(orderRepository, inventoryFacade, eventPublisher, responseAssembler)
                .execute(orderId);

        verify(inventoryFacade, never()).releaseReservationsForSalesOrder(any(), any());
        verify(eventPublisher, never()).salesOrderCancelled(any());
    }

    @Test
    void shouldReleaseReservationsWhenCancellingConfirmedOrder() {
        UUID tenantId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        TenantContext.setTenantId(tenantId);
        SalesOrder order = order(tenantId);
        order.confirm();
        when(orderRepository.findByIdAndSellerCompanyId(orderId, tenantId)).thenReturn(Optional.of(order));

        new CancelSalesOrderUseCase(orderRepository, inventoryFacade, eventPublisher, responseAssembler)
                .execute(orderId);

        verify(inventoryFacade).releaseReservationsForSalesOrder(tenantId, order.getId());
        verify(eventPublisher).salesOrderCancelled(order);
    }

    private static SalesOrder order(UUID sellerCompanyId) {
        return SalesOrder.createB2C(
                100L,
                FulfillmentType.PICKUP,
                PaymentMethod.PIX,
                null,
                sellerCompanyId,
                UUID.randomUUID(),
                Set.of(SalesOrderItem.create(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "SKU-001",
                        "Product",
                        1,
                        Money.of(BigDecimal.TEN, "BRL"))));
    }
}
