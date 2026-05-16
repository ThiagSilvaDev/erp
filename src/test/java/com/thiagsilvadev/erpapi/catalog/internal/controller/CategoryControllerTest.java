package com.thiagsilvadev.erpapi.catalog.internal.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

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
import com.thiagsilvadev.erpapi.catalog.internal.dto.category.CategoryResponse;
import com.thiagsilvadev.erpapi.catalog.internal.dto.category.CategorySearchCriteria;
import com.thiagsilvadev.erpapi.catalog.internal.dto.category.CreateCategoryRequest;
import com.thiagsilvadev.erpapi.catalog.internal.usecase.category.CreateCategoryUseCase;
import com.thiagsilvadev.erpapi.catalog.internal.usecase.category.GetCategoryByIdUseCase;
import com.thiagsilvadev.erpapi.catalog.internal.usecase.category.ListCategoriesUseCase;

class CategoryControllerTest {

    private CreateCategoryUseCase createCategoryUseCase;
    private GetCategoryByIdUseCase getCategoryByIdUseCase;
    private ListCategoriesUseCase listCategoriesUseCase;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        createCategoryUseCase = org.mockito.Mockito.mock(CreateCategoryUseCase.class);
        getCategoryByIdUseCase = org.mockito.Mockito.mock(GetCategoryByIdUseCase.class);
        listCategoriesUseCase = org.mockito.Mockito.mock(ListCategoriesUseCase.class);
        mockMvc = standaloneSetup(
                        new CategoryController(createCategoryUseCase, getCategoryByIdUseCase, listCategoriesUseCase))
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void shouldCreateCategory() throws Exception {
        UUID id = UUID.randomUUID();
        when(createCategoryUseCase.execute(any(CreateCategoryRequest.class)))
                .thenReturn(new CategoryResponse(id, "RAW", "Raw materials", null, CategoryStatus.ACTIVE));

        mockMvc.perform(post("/api/v1/catalog/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"raw\",\"name\":\"Raw materials\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/catalog/categories/" + id))
                .andExpect(jsonPath("$.code").value("RAW"))
                .andExpect(jsonPath("$.name").value("Raw materials"));
    }

    @Test
    void shouldGetCategoryById() throws Exception {
        UUID id = UUID.randomUUID();
        when(getCategoryByIdUseCase.execute(id))
                .thenReturn(new CategoryResponse(id, "RAW", "Raw materials", null, CategoryStatus.ACTIVE));

        mockMvc.perform(get("/api/v1/catalog/categories/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldListCategories() throws Exception {
        UUID id = UUID.randomUUID();
        when(listCategoriesUseCase.execute(any(CategorySearchCriteria.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(
                        List.of(new CategoryResponse(id, "RAW", "Raw materials", null, CategoryStatus.ACTIVE)),
                        PageRequest.of(0, 20),
                        1));

        mockMvc.perform(get("/api/v1/catalog/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].code").value("RAW"));
    }
}
