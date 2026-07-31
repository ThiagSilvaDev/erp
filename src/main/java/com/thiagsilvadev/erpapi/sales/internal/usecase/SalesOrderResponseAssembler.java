package com.thiagsilvadev.erpapi.sales.internal.usecase;

import org.springframework.stereotype.Component;

import com.thiagsilvadev.erpapi.company.api.CompanyFacade;
import com.thiagsilvadev.erpapi.company.api.CompanySnapshot;
import com.thiagsilvadev.erpapi.customer.api.CustomerFacade;
import com.thiagsilvadev.erpapi.customer.api.CustomerSnapshot;
import com.thiagsilvadev.erpapi.sales.api.SaleType;
import com.thiagsilvadev.erpapi.sales.internal.dto.SalesMapper;
import com.thiagsilvadev.erpapi.sales.internal.dto.SalesOrderBuyerSummaryResponse;
import com.thiagsilvadev.erpapi.sales.internal.dto.SalesOrderResponse;
import com.thiagsilvadev.erpapi.sales.internal.dto.SalesOrderSellerSummaryResponse;
import com.thiagsilvadev.erpapi.sales.internal.model.SalesOrder;

@Component
class SalesOrderResponseAssembler {

    private final SalesMapper mapper;
    private final CustomerFacade customerFacade;
    private final CompanyFacade companyFacade;

    SalesOrderResponseAssembler(SalesMapper mapper, CustomerFacade customerFacade, CompanyFacade companyFacade) {
        this.mapper = mapper;
        this.customerFacade = customerFacade;
        this.companyFacade = companyFacade;
    }

    SalesOrderResponse toResponse(SalesOrder order) {
        SalesOrderResponse response = mapper.toResponse(order);
        SalesOrderSellerSummaryResponse sellerSummary = sellerSummary(order);
        SalesOrderBuyerSummaryResponse buyerSummary = buyerSummary(order);

        return new SalesOrderResponse(
                response.id(),
                response.orderNumber(),
                response.saleType(),
                response.fulfillmentType(),
                response.paymentMethod(),
                response.deliveryRoute(),
                response.sellerCompanyId(),
                sellerSummary,
                response.buyerCompanyId(),
                response.customerId(),
                buyerSummary,
                response.status(),
                response.totalAmount(),
                response.currencyCode(),
                response.formattedTotalAmount(),
                response.items());
    }

    private SalesOrderSellerSummaryResponse sellerSummary(SalesOrder order) {
        CompanySnapshot company = companyFacade.getCompany(order.getSellerCompanyId());

        return new SalesOrderSellerSummaryResponse(companyName(company));
    }

    private SalesOrderBuyerSummaryResponse buyerSummary(SalesOrder order) {
        if (order.getSaleType() == SaleType.B2B) {
            CompanySnapshot company = companyFacade.getCompany(order.getBuyerCompanyId());

            String buyerName = company.primaryContact() == null
                    ? null
                    : company.primaryContact().name();
            return new SalesOrderBuyerSummaryResponse(buyerName, companyName(company));
        }

        CustomerSnapshot customer = customerFacade.getCustomer(order.getSellerCompanyId(), order.getCustomerId());
        return new SalesOrderBuyerSummaryResponse(customer.name(), null);
    }

    private String companyName(CompanySnapshot company) {
        String tradeName = trimToNull(company.tradeName());
        return tradeName == null ? company.legalName() : tradeName;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }
}
