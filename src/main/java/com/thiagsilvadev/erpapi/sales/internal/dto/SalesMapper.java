package com.thiagsilvadev.erpapi.sales.internal.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.thiagsilvadev.erpapi.sales.api.SalesOrderSnapshot;
import com.thiagsilvadev.erpapi.sales.internal.dto.SalesDtos.*;
import com.thiagsilvadev.erpapi.sales.internal.model.*;

@Mapper(componentModel = "spring")
public interface SalesMapper {

    @Mapping(target = "totalAmount", expression = "java(order.totalAmount())")
    @Mapping(target = "currencyCode", expression = "java(order.currencyCode())")
    SalesOrderResponse toResponse(SalesOrder order);

    @Mapping(target = "lineTotal", expression = "java(item.lineTotal())")
    SalesOrderItemResponse toResponse(SalesOrderItem item);

    @Mapping(target = "totalAmount", expression = "java(order.totalAmount())")
    @Mapping(target = "currencyCode", expression = "java(order.currencyCode())")
    SalesOrderSnapshot toSnapshot(SalesOrder order);
}
