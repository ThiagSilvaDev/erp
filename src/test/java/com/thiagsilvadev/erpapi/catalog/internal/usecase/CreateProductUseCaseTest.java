package com.thiagsilvadev.erpapi.catalog.internal.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.thiagsilvadev.erpapi.catalog.internal.dto.MoneyRequest;
import com.thiagsilvadev.erpapi.catalog.internal.dto.product.CreateProductRequest;
import com.thiagsilvadev.erpapi.catalog.internal.event.CatalogEventPublisher;
import com.thiagsilvadev.erpapi.catalog.internal.exception.SkuAlreadyExistsException;
import com.thiagsilvadev.erpapi.catalog.internal.mapper.CatalogMapper;
import com.thiagsilvadev.erpapi.catalog.internal.model.Category;
import com.thiagsilvadev.erpapi.catalog.internal.model.Sku;
import com.thiagsilvadev.erpapi.catalog.internal.repository.CategoryRepository;
import com.thiagsilvadev.erpapi.catalog.internal.repository.ProductRepository;
import com.thiagsilvadev.erpapi.catalog.internal.service.SkuAssignmentService;
import com.thiagsilvadev.erpapi.catalog.internal.usecase.product.CreateProductUseCase;
import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;

@ExtendWith(MockitoExtension.class)
class CreateProductUseCaseTest {
    private static final UUID COMPANY_ID = UUID.randomUUID();

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CatalogMapper catalogMapper;

    @Mock
    private CatalogEventPublisher eventPublisher;

    @Mock
    private SkuAssignmentService skuAssignmentService;

    @InjectMocks
    private CreateProductUseCase useCase;

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldRejectDuplicatedSku() {
        UUID categoryId = UUID.randomUUID();
        TenantContext.setTenantId(COMPANY_ID);
        CreateProductRequest request = new CreateProductRequest(
                " sku-1 ", "Product", null, new MoneyRequest(java.math.BigDecimal.TEN, "BRL"), categoryId);
        Category category = Category.create(COMPANY_ID, "RAW", "Raw materials", null);
        when(categoryRepository.findByIdAndCompanyId(categoryId, COMPANY_ID)).thenReturn(Optional.of(category));
        when(skuAssignmentService.assign(any(), any())).thenThrow(new SkuAlreadyExistsException("SKU-1"));

        assertThatThrownBy(() -> useCase.execute(request)).isInstanceOf(SkuAlreadyExistsException.class);

        verify(productRepository, never()).saveAndFlush(org.mockito.ArgumentMatchers.any());
        verify(eventPublisher, never()).productCreated(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldRejectMissingCategory() {
        UUID categoryId = UUID.randomUUID();
        TenantContext.setTenantId(COMPANY_ID);
        CreateProductRequest request = new CreateProductRequest(
                "sku-1", "Product", null, new MoneyRequest(java.math.BigDecimal.TEN, "BRL"), categoryId);
        when(categoryRepository.findByIdAndCompanyId(categoryId, COMPANY_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(request)).isInstanceOf(ResourceNotFoundException.class);

        verify(productRepository, never()).saveAndFlush(org.mockito.ArgumentMatchers.any());
        verify(eventPublisher, never()).productCreated(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldTranslateDuplicateSkuRace() {
        UUID categoryId = UUID.randomUUID();
        TenantContext.setTenantId(COMPANY_ID);
        Category category = Category.create(COMPANY_ID, "RAW", "Raw materials", null);
        CreateProductRequest request = new CreateProductRequest(
                "sku-1", "Product", null, new MoneyRequest(java.math.BigDecimal.TEN, "BRL"), categoryId);
        when(categoryRepository.findByIdAndCompanyId(categoryId, COMPANY_ID)).thenReturn(Optional.of(category));
        when(skuAssignmentService.assign(any(), any())).thenReturn(Sku.of("SKU-1"));
        when(productRepository.saveAndFlush(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new DataIntegrityViolationException(
                        "duplicate",
                        new ConstraintViolationException("duplicate", null, "uk_catalog_product_company_sku")));

        assertThatThrownBy(() -> useCase.execute(request)).isInstanceOf(SkuAlreadyExistsException.class);

        verify(eventPublisher, never()).productCreated(org.mockito.ArgumentMatchers.any());
    }
}
