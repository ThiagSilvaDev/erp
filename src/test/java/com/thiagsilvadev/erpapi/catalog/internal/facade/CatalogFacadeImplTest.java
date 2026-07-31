package com.thiagsilvadev.erpapi.catalog.internal.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.thiagsilvadev.erpapi.catalog.api.ProductSnapshot;
import com.thiagsilvadev.erpapi.catalog.internal.mapper.CatalogMapper;
import com.thiagsilvadev.erpapi.catalog.internal.model.Product;
import com.thiagsilvadev.erpapi.catalog.internal.repository.ProductRepository;
import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;

class CatalogFacadeImplTest {

    private final ProductRepository productRepository = mock(ProductRepository.class);
    private final CatalogMapper catalogMapper = mock(CatalogMapper.class);
    private final CatalogFacadeImpl facade = new CatalogFacadeImpl(productRepository, catalogMapper);

    @Test
    void shouldGetTenantScopedProduct() {
        UUID companyId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        Product product = mock(Product.class);
        ProductSnapshot snapshot = mock(ProductSnapshot.class);
        when(productRepository.findByIdAndCompanyId(productId, companyId)).thenReturn(Optional.of(product));
        when(catalogMapper.toSnapshot(product)).thenReturn(snapshot);

        assertThat(facade.getProduct(companyId, productId)).isSameAs(snapshot);
    }

    @Test
    void shouldNormalizeSkuWhenResolvingProduct() {
        UUID companyId = UUID.randomUUID();
        Product product = mock(Product.class);
        ProductSnapshot snapshot = mock(ProductSnapshot.class);
        when(productRepository.findByCompanyIdAndSkuValue(companyId, "SKU-001")).thenReturn(Optional.of(product));
        when(catalogMapper.toSnapshot(product)).thenReturn(snapshot);

        assertThat(facade.resolveProduct(companyId, "sku-001")).isSameAs(snapshot);
        verify(productRepository).findByCompanyIdAndSkuValue(companyId, "SKU-001");
    }

    @Test
    void shouldRejectBatchWhenAnyProductIsMissing() {
        UUID companyId = UUID.randomUUID();
        UUID foundId = UUID.randomUUID();
        UUID missingId = UUID.randomUUID();
        Product product = mock(Product.class);
        when(product.getId()).thenReturn(foundId);
        when(productRepository.findAllByCompanyIdAndIdIn(companyId, Set.of(foundId, missingId)))
                .thenReturn(List.of(product));

        assertThatThrownBy(() -> facade.getProducts(companyId, Set.of(foundId, missingId)))
                .isInstanceOf(ResourceNotFoundException.class)
                .satisfies(exception -> assertThat(((ResourceNotFoundException) exception).getProperties())
                        .containsEntry("resourceName", "Product"));
    }

    @Test
    void shouldRejectMissingProduct() {
        UUID companyId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        when(productRepository.findByIdAndCompanyId(productId, companyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> facade.getProduct(companyId, productId)).isInstanceOf(ResourceNotFoundException.class);
    }
}
