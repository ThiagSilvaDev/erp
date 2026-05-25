package com.thiagsilvadev.erpapi.sales.internal.exception;

import com.thiagsilvadev.erpapi.common.domain.exception.DomainErrorCode;

public enum DeliveryRouteErrorCode implements DomainErrorCode {
    REQUIRED_FOR_DELIVERY,
    NOT_ALLOWED_FOR_PICKUP,
    ITEMS_MUST_USE_SINGLE_ORIGIN_WAREHOUSE;

    @Override
    public String code() {
        return "DELIVERY_ROUTE_" + name();
    }
}
