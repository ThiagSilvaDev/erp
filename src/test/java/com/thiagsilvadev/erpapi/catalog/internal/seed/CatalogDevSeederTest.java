package com.thiagsilvadev.erpapi.catalog.internal.seed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.thiagsilvadev.erpapi.catalog.internal.model.Category;
import com.thiagsilvadev.erpapi.catalog.internal.model.Product;
import com.thiagsilvadev.erpapi.catalog.internal.repository.CategoryRepository;
import com.thiagsilvadev.erpapi.catalog.internal.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class CatalogDevSeederTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Test
    void shouldSeedCategoryAndProductWhenMissing() {
        Category category = Category.create(CatalogDevSeeder.INDUSTRIAL_CATEGORY_CODE, "Industrial Supplies", null);
        when(categoryRepository.findByCode("INDUSTRIAL")).thenReturn(Optional.empty());
        when(categoryRepository.save(org.mockito.ArgumentMatchers.any(Category.class)))
                .thenReturn(category);
        when(productRepository.existsBySku("SKU-001")).thenReturn(false);

        new CatalogDevSeeder(categoryRepository, productRepository).run(null);

        verify(categoryRepository).save(org.mockito.ArgumentMatchers.any(Category.class));

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());

        Product product = captor.getValue();
        assertThat(product.getSku()).isEqualTo("SKU-001");
        assertThat(product.getName()).isEqualTo("Industrial Widget");
        assertThat(product.getCategory()).isEqualTo(category);
        assertThat(product.getBasePrice().getAmount()).isEqualByComparingTo("10.50");
    }

    @Test
    void shouldSkipProductWhenAlreadySeeded() {
        Category category = Category.create(CatalogDevSeeder.INDUSTRIAL_CATEGORY_CODE, "Industrial Supplies", null);
        when(categoryRepository.findByCode("INDUSTRIAL")).thenReturn(Optional.of(category));
        when(productRepository.existsBySku("SKU-001")).thenReturn(true);

        new CatalogDevSeeder(categoryRepository, productRepository).run(null);

        verify(categoryRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(productRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
