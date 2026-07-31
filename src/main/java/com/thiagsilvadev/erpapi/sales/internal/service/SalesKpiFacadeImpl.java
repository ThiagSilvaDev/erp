package com.thiagsilvadev.erpapi.sales.internal.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.reporting.ComparativePeriod;
import com.thiagsilvadev.erpapi.sales.api.SalesKpiFacade;
import com.thiagsilvadev.erpapi.sales.api.SalesKpiSnapshot;
import com.thiagsilvadev.erpapi.sales.internal.repository.SalesOrderRepository;

@Service
class SalesKpiFacadeImpl implements SalesKpiFacade {

    private final SalesOrderRepository orderRepository;
    private final SalesKpiCalculator calculator;

    SalesKpiFacadeImpl(SalesOrderRepository orderRepository, SalesKpiCalculator calculator) {
        this.orderRepository = orderRepository;
        this.calculator = calculator;
    }

    @Override
    @Transactional(readOnly = true)
    public SalesKpiSnapshot calculate(UUID companyId, ComparativePeriod period) {
        var rows =
                orderRepository.findKpiRows(companyId, period.queryFromInclusive(), period.queryToExclusive()).stream()
                        .map(row -> new SalesKpiCalculator.DailyRow(
                                row.getDate(),
                                row.getCurrencyCode(),
                                row.getOrders(),
                                row.getConfirmedOrders(),
                                row.getCancelledOrders(),
                                row.getConfirmedRevenue()))
                        .toList();
        return calculator.calculate(rows, period);
    }
}
