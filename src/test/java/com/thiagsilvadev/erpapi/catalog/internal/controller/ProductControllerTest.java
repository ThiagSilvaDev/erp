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

import com.thiagsilvadev.erpapi.catalog.api.ProductStatus;
import com.thiagsilvadev.erpapi.catalog.internal.dto.MoneyResponse;
import com.thiagsilvadev.erpapi.catalog.internal.dto.category.CategorySummaryResponse;
import com.thiagsilvadev.erpapi.catalog.internal.dto.product.CreateProductRequest;
import com.thiagsilvadev.erpapi.catalog.internal.dto.product.ProductResponse;
import com.thiagsilvadev.erpapi.catalog.internal.dto.product.ProductSearchCriteria;
import com.thiagsilvadev.erpapi.catalog.internal.usecase.product.CreateProductUseCase;
import com.thiagsilvadev.erpapi.catalog.internal.usecase.product.ProductQueryUseCase;

class ProductControllerTest {

    private CreateProductUseCase createProductUseCase;
    private ProductQueryUseCase productQueryUseCase;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        createProductUseCase = org.mockito.Mockito.mock(CreateProductUseCase.class);
        productQueryUseCase = org.mockito.Mockito.mock(ProductQueryUseCase.class);
        mockMvc = standaloneSetup(new ProductController(createProductUseCase, productQueryUseCase))
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
                .andExpect(jsonPath("$.category.id").value(categoryId.toString()))
                .andExpect(jsonPath("$.category.code").value("RAW"))
                .andExpect(jsonPath("$.category.name").value("Raw materials"))
                .andExpect(jsonPath("$.category.description").doesNotExist())
                .andExpect(jsonPath("$.category.status").doesNotExist());
    }

    @Test
    void shouldGetProductById() throws Exception {
        UUID id = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        when(productQueryUseCase.getById(id)).thenReturn(productResponse(id, categoryId));

        mockMvc.perform(get("/api/v1/catalog/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldListProducts() throws Exception {
        UUID id = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        when(productQueryUseCase.list(any(ProductSearchCriteria.class), any(Pageable.class)))
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
                "BRL 10.50",
                ProductStatus.ACTIVE,
                new CategorySummaryResponse(categoryId, "RAW", "Raw materials"));
    }
}
