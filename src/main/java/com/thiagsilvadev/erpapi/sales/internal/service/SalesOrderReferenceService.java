package com.thiagsilvadev.erpapi.sales.internal.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.inventory.api.SalesOrderReference;
import com.thiagsilvadev.erpapi.inventory.api.SalesOrderReferenceProvider;
import com.thiagsilvadev.erpapi.sales.internal.repository.SalesOrderRepository;

@Service
public class SalesOrderReferenceService implements SalesOrderReferenceProvider {

    private final SalesOrderRepository orderRepository;

    public SalesOrderReferenceService(SalesOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SalesOrderReference> findSalesOrderReferenceById(UUID id) {
        return orderRepository
                .findById(id)
                .map(order -> new SalesOrderReference(order.getId(), order.getOrderNumber()));
    }
}
