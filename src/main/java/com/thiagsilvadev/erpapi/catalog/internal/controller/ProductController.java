package com.thiagsilvadev.erpapi.catalog.internal.controller;

import java.net.URI;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.thiagsilvadev.erpapi.catalog.internal.dto.CreateProductRequest;
import com.thiagsilvadev.erpapi.catalog.internal.dto.ProductResponse;
import com.thiagsilvadev.erpapi.catalog.internal.dto.ProductSearchCriteria;
import com.thiagsilvadev.erpapi.catalog.internal.usecase.CreateProductUseCase;
import com.thiagsilvadev.erpapi.catalog.internal.usecase.GetProductByIdUseCase;
import com.thiagsilvadev.erpapi.catalog.internal.usecase.ListProductsUseCase;

@RestController
@Validated
class ProductController implements ProductApi {

    private final CreateProductUseCase createProductUseCase;
    private final GetProductByIdUseCase getProductByIdUseCase;
    private final ListProductsUseCase listProductsUseCase;

    ProductController(
            CreateProductUseCase createProductUseCase,
            GetProductByIdUseCase getProductByIdUseCase,
            ListProductsUseCase listProductsUseCase) {
        this.createProductUseCase = createProductUseCase;
        this.getProductByIdUseCase = getProductByIdUseCase;
        this.listProductsUseCase = listProductsUseCase;
    }

    @Override
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        ProductResponse response = createProductUseCase.execute(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.status(HttpStatus.CREATED).location(location).body(response);
    }

    @Override
    public ResponseEntity<ProductResponse> getProductById(@PathVariable UUID id) {
        return ResponseEntity.ok(getProductByIdUseCase.execute(id));
    }

    @Override
    public ResponseEntity<Page<ProductResponse>> listProducts(ProductSearchCriteria criteria, Pageable pageable) {
        return ResponseEntity.ok(listProductsUseCase.execute(criteria, pageable));
    }
}
