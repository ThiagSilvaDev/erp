package com.thiagsilvadev.erpapi.catalog.internal.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.thiagsilvadev.erpapi.catalog.api.CategoryStatus;
import com.thiagsilvadev.erpapi.catalog.api.ProductStatus;
import com.thiagsilvadev.erpapi.catalog.internal.dto.MoneyResponse;
import com.thiagsilvadev.erpapi.catalog.internal.dto.category.CategoryResponse;
import com.thiagsilvadev.erpapi.catalog.internal.dto.product.CreateProductRequest;
import com.thiagsilvadev.erpapi.catalog.internal.dto.product.ProductResponse;
import com.thiagsilvadev.erpapi.catalog.internal.dto.product.ProductSearchCriteria;
import com.thiagsilvadev.erpapi.catalog.internal.usecase.product.CreateProductUseCase;
import com.thiagsilvadev.erpapi.catalog.internal.usecase.product.GetProductByIdUseCase;
import com.thiagsilvadev.erpapi.catalog.internal.usecase.product.ListProductsUseCase;

class ProductControllerTest {

    private CreateProductUseCase createProductUseCase;
    private GetProductByIdUseCase getProductByIdUseCase;
    private ListProductsUseCase listProductsUseCase;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        createProductUseCase = org.mockito.Mockito.mock(CreateProductUseCase.class);
        getProductByIdUseCase = org.mockito.Mockito.mock(GetProductByIdUseCase.class);
        listProductsUseCase = org.mockito.Mockito.mock(ListProductsUseCase.class);
        mockMvc = standaloneSetup(
                        new ProductController(createProductUseCase, getProductByIdUseCase, listProductsUseCase))
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void shouldCreateProduct() throws Exception {
        UUID id = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        when(createProductUseCase.execute(any(CreateProductRequest.class))).thenReturn(productResponse(id, categoryId));

        mockMvc.perform(post("/api/v1/catalog/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {
                                  "sku": "sku-1",
                                  "name": "Product",
                                  "basePrice": {
                                    "amount": 10.50,
                                    "currencyCode": "BRL"
                                  },
                                  "categoryId": "%s"
                                }
                                """
                                        .formatted(categoryId)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/catalog/products/" + id))
                .andExpect(jsonPath("$.sku").value("SKU-1"))
                .andExpect(jsonPath("$.basePrice.amount").value(10.50))
                .andExpect(jsonPath("$.basePrice.currencyCode").value("BRL"))
                .andExpect(jsonPath("$.category.code").value("RAW"));
    }

    @Test
    void shouldGetProductById() throws Exception {
        UUID id = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        when(getProductByIdUseCase.execute(id)).thenReturn(productResponse(id, categoryId));

        mockMvc.perform(get("/api/v1/catalog/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldListProducts() throws Exception {
        UUID id = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        when(listProductsUseCase.execute(any(ProductSearchCriteria.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(productResponse(id, categoryId)), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/v1/catalog/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].sku").value("SKU-1"));
    }

    private static ProductResponse productResponse(UUID id, UUID categoryId) {
        return new ProductResponse(
                id,
                "SKU-1",
                "Product",
                null,
                new MoneyResponse(new BigDecimal("10.50"), "BRL"),
                ProductStatus.ACTIVE,
                new CategoryResponse(categoryId, "RAW", "Raw materials", null, CategoryStatus.ACTIVE));
    }
}
