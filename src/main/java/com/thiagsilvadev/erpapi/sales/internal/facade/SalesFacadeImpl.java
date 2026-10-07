package com.thiagsilvadev.erpapi.sales.internal.facade;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.sales.api.SalesFacade;
import com.thiagsilvadev.erpapi.sales.api.SalesOrderSnapshot;
import com.thiagsilvadev.erpapi.sales.internal.dto.SalesMapper;
import com.thiagsilvadev.erpapi.sales.internal.model.SalesOrder;
import com.thiagsilvadev.erpapi.sales.internal.repository.SalesOrderRepository;

@Service
public class SalesFacadeImpl implements SalesFacade {

    private final SalesOrderRepository salesOrderRepository;
    private final SalesMapper salesMapper;

    public SalesFacadeImpl(SalesOrderRepository salesOrderRepository, SalesMapper salesMapper) {
        this.salesOrderRepository = salesOrderRepository;
        this.salesMapper = salesMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public SalesOrderSnapshot getOrder(UUID sellerCompanyId, UUID salesOrderId) {
        return salesOrderRepository
                .findByIdAndSellerCompanyId(salesOrderId, sellerCompanyId)
                .map(salesMapper::toSnapshot)
                .orElseThrow(() -> new ResourceNotFoundException(SalesOrder.class, salesOrderId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesOrderSnapshot> listOrders(UUID sellerCompanyId) {
        return salesOrderRepository.findAllBySellerCompanyId(sellerCompanyId).stream()
                .sorted(Comparator.comparing(SalesOrder::getOrderNumber))
                .map(salesMapper::toSnapshot)
                .toList();
    }
}
