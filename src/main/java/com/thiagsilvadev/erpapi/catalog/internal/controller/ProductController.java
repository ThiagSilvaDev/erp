package com.thiagsilvadev.erpapi.catalog.internal.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.thiagsilvadev.erpapi.catalog.internal.dto.product.CreateProductRequest;
import com.thiagsilvadev.erpapi.catalog.internal.dto.product.ProductResponse;
import com.thiagsilvadev.erpapi.catalog.internal.dto.product.ProductSearchCriteria;
import com.thiagsilvadev.erpapi.catalog.internal.usecase.product.CreateProductUseCase;
import com.thiagsilvadev.erpapi.catalog.internal.usecase.product.ProductQueryUseCase;
import com.thiagsilvadev.erpapi.common.web.ResponseEntities;

@RestController
@Validated
class ProductController implements ProductApi {

    private final CreateProductUseCase createProductUseCase;
    private final ProductQueryUseCase productQueryUseCase;

    ProductController(CreateProductUseCase createProductUseCase, ProductQueryUseCase productQueryUseCase) {
        this.createProductUseCase = createProductUseCase;
        this.productQueryUseCase = productQueryUseCase;
    }

    @Override
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request) {
        ProductResponse response = createProductUseCase.execute(request);
        return ResponseEntities.created(response.id(), response);
    }

    @Override
    public ProductResponse getById(@PathVariable UUID id) {
        return productQueryUseCase.getById(id);
    }

    @Override
    public Page<ProductResponse> list(ProductSearchCriteria criteria, Pageable pageable) {
        return productQueryUseCase.list(criteria, pageable);
    }
}
