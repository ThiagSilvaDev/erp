package com.thiagsilvadev.erpapi.catalog.internal.seed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
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
        Category industrialCategory =
                Category.create(CatalogDevSeeder.INDUSTRIAL_CATEGORY_CODE, "Industrial Supplies", null);
        Category officeCategory = Category.create(CatalogDevSeeder.OFFICE_CATEGORY_CODE, "Office Supplies", null);
        when(categoryRepository.findByCode("INDUSTRIAL")).thenReturn(Optional.empty());
        when(categoryRepository.findByCode("OFFICE")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(industrialCategory, officeCategory);
        seedSkus().forEach(sku -> when(productRepository.existsBySkuValue(sku)).thenReturn(false));

        new CatalogDevSeeder(categoryRepository, productRepository).run(null);

        verify(categoryRepository, times(2)).save(any(Category.class));

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository, times(10)).save(captor.capture());

        List<Product> products = captor.getAllValues();
        assertThat(products).extracting(Product::getSku).containsExactlyElementsOf(seedSkus());
        assertThat(products)
                .filteredOn(product -> product.getSku().equals("SKU-001"))
                .singleElement()
                .satisfies(product -> {
                    assertThat(product.getName()).isEqualTo("Industrial Widget");
                    assertThat(product.getCategory()).isEqualTo(industrialCategory);
                    assertThat(product.getBasePrice().getAmount()).isEqualByComparingTo("10.50");
                });
        assertThat(products)
                .filteredOn(product -> product.getSku().equals("SKU-010"))
                .singleElement()
                .satisfies(product -> {
                    assertThat(product.getName()).isEqualTo("Shipping Labels");
                    assertThat(product.getCategory()).isEqualTo(officeCategory);
                    assertThat(product.getBasePrice().getAmount()).isEqualByComparingTo("16.80");
                });
    }

    @Test
    void shouldSkipProductWhenAlreadySeeded() {
        Category industrialCategory =
                Category.create(CatalogDevSeeder.INDUSTRIAL_CATEGORY_CODE, "Industrial Supplies", null);
        Category officeCategory = Category.create(CatalogDevSeeder.OFFICE_CATEGORY_CODE, "Office Supplies", null);
        when(categoryRepository.findByCode("INDUSTRIAL")).thenReturn(Optional.of(industrialCategory));
        when(categoryRepository.findByCode("OFFICE")).thenReturn(Optional.of(officeCategory));
        seedSkus().forEach(sku -> when(productRepository.existsBySkuValue(sku)).thenReturn(true));

        new CatalogDevSeeder(categoryRepository, productRepository).run(null);

        verify(categoryRepository, never()).save(any());
        verify(productRepository, never()).save(any());
    }

    private static List<String> seedSkus() {
        return List.of(
                "SKU-001", "SKU-002", "SKU-003", "SKU-004", "SKU-005", "SKU-006", "SKU-007", "SKU-008", "SKU-009",
                "SKU-010");
    }
}
