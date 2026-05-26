package com.thiagsilvadev.erpapi.sales.internal.usecase;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.catalog.api.CatalogFacade;
import com.thiagsilvadev.erpapi.catalog.api.ProductSnapshot;
import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.domain.model.GeoLocation;
import com.thiagsilvadev.erpapi.common.domain.numbering.BusinessNumberService;
import com.thiagsilvadev.erpapi.common.domain.numbering.BusinessNumberType;
import com.thiagsilvadev.erpapi.company.api.CompanyFacade;
import com.thiagsilvadev.erpapi.company.api.CompanySnapshot;
import com.thiagsilvadev.erpapi.company.api.CompanyType;
import com.thiagsilvadev.erpapi.customer.api.CustomerFacade;
import com.thiagsilvadev.erpapi.customer.api.CustomerSnapshot;
import com.thiagsilvadev.erpapi.sales.api.SaleType;
import com.thiagsilvadev.erpapi.sales.internal.dto.*;
import com.thiagsilvadev.erpapi.sales.internal.model.DeliveryRoute;
import com.thiagsilvadev.erpapi.sales.internal.model.SalesOrder;
import com.thiagsilvadev.erpapi.sales.internal.model.SalesOrderItem;
import com.thiagsilvadev.erpapi.sales.internal.repository.SalesOrderRepository;

@Component
public class CreateSalesOrderUseCase {

    private final SalesOrderRepository orderRepository;
    private final CustomerFacade customerFacade;
    private final CompanyFacade companyFacade;
    private final CatalogFacade catalogFacade;
    private final SalesMapper mapper;
    private final BusinessNumberService businessNumberService;

    public CreateSalesOrderUseCase(
            SalesOrderRepository orderRepository,
            CustomerFacade customerFacade,
            CompanyFacade companyFacade,
            CatalogFacade catalogFacade,
            SalesMapper mapper,
            BusinessNumberService businessNumberService) {
        this.orderRepository = orderRepository;
        this.customerFacade = customerFacade;
        this.companyFacade = companyFacade;
        this.catalogFacade = catalogFacade;
        this.mapper = mapper;
        this.businessNumberService = businessNumberService;
    }

    @Transactional
    @PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.sales.api.SalesPermissions).WRITE)")
    public SalesOrderResponse execute(CreateSalesOrderRequest request) {
        validateParties(request);

        Set<UUID> productIds = request.items().stream()
                .map(CreateSalesOrderItemRequest::productId)
                .collect(Collectors.toSet());

        Map<UUID, ProductSnapshot> productMap = catalogFacade.findAllProductsByIdIn(productIds).stream()
                .collect(Collectors.toMap(ProductSnapshot::id, p -> p));

        var items = request.items().stream()
                .map(item -> {
                    ProductSnapshot product = Optional.ofNullable(productMap.get(item.productId()))
                            .orElseThrow(() -> new ResourceNotFoundException("Product", item.productId()));

                    return SalesOrderItem.create(
                            product.id(),
                            item.warehouseId(),
                            product.sku(),
                            product.name(),
                            item.quantity(),
                            product.basePrice());
                })
                .collect(Collectors.toCollection(LinkedHashSet::new));

        DeliveryRoute deliveryRoute = toDeliveryRoute(request.deliveryRoute());

        Long orderNumber = businessNumberService.next(BusinessNumberType.SALES_ORDER);
        SalesOrder order =
                switch (request.saleType()) {
                    case B2B ->
                        SalesOrder.createB2B(
                                orderNumber,
                                request.fulfillmentType(),
                                request.paymentMethod(),
                                deliveryRoute,
                                request.sellerCompanyId(),
                                request.buyerCompanyId(),
                                items);
                    case B2C ->
                        SalesOrder.createB2C(
                                orderNumber,
                                request.fulfillmentType(),
                                request.paymentMethod(),
                                deliveryRoute,
                                request.sellerCompanyId(),
                                request.customerId(),
                                items);
                };
        orderRepository.save(order);

        return mapper.toResponse(order);
    }

    private void validateParties(CreateSalesOrderRequest request) {
        CompanySnapshot seller = companyFacade
                .findById(request.sellerCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company", request.sellerCompanyId()));

        if (request.saleType() == SaleType.B2B) {
            validateB2b(request, seller);
            return;
        }

        validateB2c(request, seller);
    }

    private void validateB2b(CreateSalesOrderRequest request, CompanySnapshot seller) {
        if (request.buyerCompanyId() == null) {
            throw new IllegalArgumentException("Buyer company is required for B2B sales");
        }
        if (request.customerId() != null) {
            throw new IllegalArgumentException("Customer must be null for B2B sales");
        }
        if (seller.type() != CompanyType.FACTORY) {
            throw new IllegalArgumentException("B2B seller must be a factory");
        }
        if (request.sellerCompanyId().equals(request.buyerCompanyId())) {
            throw new IllegalArgumentException("B2B buyer company cannot be the seller company");
        }

        CompanySnapshot buyer = companyFacade
                .findById(request.buyerCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company", request.buyerCompanyId()));
        if (buyer.type() != CompanyType.CORRESPONDENT) {
            throw new IllegalArgumentException("B2B buyer company must be a correspondent");
        }
    }

    private void validateB2c(CreateSalesOrderRequest request, CompanySnapshot seller) {
        if (request.buyerCompanyId() != null) {
            throw new IllegalArgumentException("Buyer company must be null for B2C sales");
        }
        if (request.customerId() == null) {
            throw new IllegalArgumentException("Customer is required for B2C sales");
        }
        if (seller.type() != CompanyType.FACTORY && seller.type() != CompanyType.CORRESPONDENT) {
            throw new IllegalArgumentException("B2C seller must be a factory or correspondent");
        }

        CustomerSnapshot customer = customerFacade
                .findCustomerById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", request.customerId()));
        if (!request.sellerCompanyId().equals(customer.ownerCompanyId())) {
            throw new IllegalArgumentException("B2C customer must belong to the seller company");
        }
    }

    private DeliveryRoute toDeliveryRoute(DeliveryRouteRequest request) {
        if (request == null) {
            return null;
        }

        return DeliveryRoute.of(
                request.routeName(),
                request.originWarehouseId(),
                request.destinationDescription(),
                toGeoLocation(request.destinationLocation()));
    }

    private GeoLocation toGeoLocation(GeoLocationRequest request) {
        if (request == null) {
            return null;
        }

        return GeoLocation.of(request.latitude(), request.longitude());
    }
}
