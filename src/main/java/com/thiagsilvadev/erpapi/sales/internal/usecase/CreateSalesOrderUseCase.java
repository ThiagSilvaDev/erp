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
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.company.api.CompanyFacade;
import com.thiagsilvadev.erpapi.company.api.CompanySnapshot;
import com.thiagsilvadev.erpapi.company.api.CompanyType;
import com.thiagsilvadev.erpapi.customer.api.CustomerFacade;
import com.thiagsilvadev.erpapi.customer.api.CustomerSnapshot;
import com.thiagsilvadev.erpapi.sales.api.SaleType;
import com.thiagsilvadev.erpapi.sales.internal.dto.*;
import com.thiagsilvadev.erpapi.sales.internal.model.DeliveryRoute;
import com.thiagsilvadev.erpapi.sales.internal.model.SalesBuyer;
import com.thiagsilvadev.erpapi.sales.internal.model.SalesOrder;
import com.thiagsilvadev.erpapi.sales.internal.model.SalesOrderItem;
import com.thiagsilvadev.erpapi.sales.internal.repository.SalesOrderRepository;

@Component
public class CreateSalesOrderUseCase {

    private final SalesOrderRepository orderRepository;
    private final CustomerFacade customerFacade;
    private final CompanyFacade companyFacade;
    private final CatalogFacade catalogFacade;
    private final SalesOrderResponseAssembler responseAssembler;
    private final BusinessNumberService businessNumberService;

    public CreateSalesOrderUseCase(
            SalesOrderRepository orderRepository,
            CustomerFacade customerFacade,
            CompanyFacade companyFacade,
            CatalogFacade catalogFacade,
            SalesOrderResponseAssembler responseAssembler,
            BusinessNumberService businessNumberService) {
        this.orderRepository = orderRepository;
        this.customerFacade = customerFacade;
        this.companyFacade = companyFacade;
        this.catalogFacade = catalogFacade;
        this.responseAssembler = responseAssembler;
        this.businessNumberService = businessNumberService;
    }

    @Transactional
    @PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.sales.api.SalesPermissions).WRITE)")
    public SalesOrderResponse execute(CreateSalesOrderRequest request) {
        UUID sellerCompanyId = TenantContext.requireTenantId();

        SalesBuyer buyer = resolveBuyer(request, sellerCompanyId);

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
        SalesOrder order = SalesOrder.create(
                orderNumber,
                request.fulfillmentType(),
                request.paymentMethod(),
                deliveryRoute,
                sellerCompanyId,
                buyer,
                items);
        orderRepository.save(order);

        return responseAssembler.toResponse(order);
    }

    private SalesBuyer resolveBuyer(CreateSalesOrderRequest request, UUID sellerCompanyId) {
        SalesBuyer buyer = SalesBuyer.of(request.saleType(), request.buyerCompanyId(), request.customerId());
        CompanySnapshot seller = companyFacade
                .findById(sellerCompanyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", sellerCompanyId));

        if (buyer.getSaleType() == SaleType.B2B) {
            validateB2b(sellerCompanyId, buyer, seller);
            return buyer;
        }

        validateB2c(sellerCompanyId, buyer, seller);
        return buyer;
    }

    private void validateB2b(UUID sellerCompanyId, SalesBuyer buyer, CompanySnapshot seller) {
        if (seller.type() != CompanyType.FACTORY) {
            throw new IllegalArgumentException("B2B seller must be a factory");
        }
        if (sellerCompanyId.equals(buyer.getBuyerCompanyId())) {
            throw new IllegalArgumentException("B2B buyer company cannot be the seller company");
        }

        CompanySnapshot buyerCompany = companyFacade
                .findById(buyer.getBuyerCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company", buyer.getBuyerCompanyId()));
        if (buyerCompany.type() != CompanyType.CORRESPONDENT) {
            throw new IllegalArgumentException("B2B buyer company must be a correspondent");
        }
    }

    private void validateB2c(UUID sellerCompanyId, SalesBuyer buyer, CompanySnapshot seller) {
        if (seller.type() != CompanyType.FACTORY && seller.type() != CompanyType.CORRESPONDENT) {
            throw new IllegalArgumentException("B2C seller must be a factory or correspondent");
        }

        CustomerSnapshot customer = customerFacade
                .findCustomerById(buyer.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", buyer.getCustomerId()));
        if (!sellerCompanyId.equals(customer.ownerCompanyId())) {
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
