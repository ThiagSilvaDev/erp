package com.thiagsilvadev.erpapi.inventory.internal.controller;

import java.util.UUID;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.thiagsilvadev.erpapi.inventory.internal.dto.item.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping(value = "/api/v1/inventory", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "inventory")
public interface InventoryItemApi {

    @GetMapping("/items")
    @Operation(operationId = "listInventoryItems")
    @ApiResponse(responseCode = "200")
    Page<InventoryItemResponse> listItems(@ParameterObject Pageable pageable);

    @GetMapping("/items/{id}")
    @Operation(operationId = "getInventoryItemById")
    @ApiResponse(responseCode = "200")
    InventoryItemResponse getItem(@PathVariable UUID id);
}
