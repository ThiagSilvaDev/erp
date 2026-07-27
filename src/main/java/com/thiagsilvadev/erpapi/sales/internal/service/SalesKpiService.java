package com.thiagsilvadev.erpapi.sales.internal.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.sales.api.SalesKpiFacade;
import com.thiagsilvadev.erpapi.sales.api.SalesKpiSnapshot;
import com.thiagsilvadev.erpapi.sales.internal.repository.SalesOrderRepository;

@Service
class SalesKpiService implements SalesKpiFacade {

    private final SalesOrderRepository orderRepository;

    SalesKpiService(SalesOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public SalesKpiSnapshot load(UUID companyId, Instant fromInclusive, Instant toExclusive) {
        return new SalesKpiSnapshot(orderRepository.findKpiRows(companyId, fromInclusive, toExclusive).stream()
                .map(row -> new SalesKpiSnapshot.Daily(
                        row.getDate(),
                        row.getCurrencyCode(),
                        row.getOrders(),
                        row.getConfirmedOrders(),
                        row.getCancelledOrders(),
                        row.getConfirmedRevenue()))
                .toList());
    }
}
