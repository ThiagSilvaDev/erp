package com.thiagsilvadev.erpapi.sales.internal.facade;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.inventory.api.SalesOrderReference;
import com.thiagsilvadev.erpapi.inventory.api.SalesOrderReferenceProvider;
import com.thiagsilvadev.erpapi.sales.internal.model.SalesOrder;
import com.thiagsilvadev.erpapi.sales.internal.repository.SalesOrderRepository;

@Service
public class SalesOrderReferenceProviderImpl implements SalesOrderReferenceProvider {

    private final SalesOrderRepository orderRepository;

    public SalesOrderReferenceProviderImpl(SalesOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public SalesOrderReference getSalesOrderReference(UUID sellerCompanyId, UUID salesOrderId) {
        return orderRepository
                .findByIdAndSellerCompanyId(salesOrderId, sellerCompanyId)
                .map(order -> new SalesOrderReference(order.getId(), order.getOrderNumber()))
                .orElseThrow(() -> new ResourceNotFoundException(SalesOrder.class, salesOrderId));
    }
}
