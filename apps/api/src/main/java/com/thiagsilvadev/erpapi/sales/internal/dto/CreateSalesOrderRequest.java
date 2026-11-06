package com.thiagsilvadev.erpapi.sales.internal.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import com.thiagsilvadev.erpapi.sales.api.FulfillmentType;
import com.thiagsilvadev.erpapi.sales.api.PaymentMethod;
import com.thiagsilvadev.erpapi.sales.api.SaleType;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateSalesOrderRequest(
        @NotNull SaleType saleType,
        @NotNull FulfillmentType fulfillmentType,
        @Valid @Schema(description = "Required when fulfillmentType is DELIVERY; must be omitted or null when PICKUP")
                DeliveryRouteRequest deliveryRoute,
        @NotNull PaymentMethod paymentMethod,
        @Schema(description = "Required when saleType is B2B; must be null when B2C") UUID buyerCompanyId,
        @Schema(description = "Required when saleType is B2C; must be null when B2B") UUID customerId,
        @Valid @NotEmpty List<CreateSalesOrderItemRequest> items) {}
