package com.thiagsilvadev.erpapi.catalog.internal.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.thiagsilvadev.erpapi.catalog.internal.dto.CreateProductRequest;
import com.thiagsilvadev.erpapi.catalog.internal.dto.MoneyRequest;
import com.thiagsilvadev.erpapi.catalog.internal.event.CatalogEventPublisher;
import com.thiagsilvadev.erpapi.catalog.internal.exception.SkuAlreadyExistsException;
import com.thiagsilvadev.erpapi.catalog.internal.mapper.CatalogMapper;
import com.thiagsilvadev.erpapi.catalog.internal.repository.CategoryRepository;
import com.thiagsilvadev.erpapi.catalog.internal.repository.ProductRepository;
import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class CreateProductUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CatalogMapper catalogMapper;

    @Mock
    private CatalogEventPublisher eventPublisher;

    @InjectMocks
    private CreateProductUseCase useCase;

    @Test
    void shouldRejectDuplicatedSku() {
        UUID categoryId = UUID.randomUUID();
        CreateProductRequest request = new CreateProductRequest(
                " sku-1 ", "Product", null, new MoneyRequest(java.math.BigDecimal.TEN, "BRL"), categoryId);
        when(productRepository.existsBySkuValue("SKU-1")).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(request)).isInstanceOf(SkuAlreadyExistsException.class);

        verify(productRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(eventPublisher, never()).productCreated(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldRejectMissingCategory() {
        UUID categoryId = UUID.randomUUID();
        CreateProductRequest request = new CreateProductRequest(
                "sku-1", "Product", null, new MoneyRequest(java.math.BigDecimal.TEN, "BRL"), categoryId);
        when(productRepository.existsBySkuValue("SKU-1")).thenReturn(false);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(request)).isInstanceOf(ResourceNotFoundException.class);

        verify(productRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(eventPublisher, never()).productCreated(org.mockito.ArgumentMatchers.any());
    }
}
