package com.thiagsilvadev.erpapi.catalog.internal.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.thiagsilvadev.erpapi.catalog.internal.model.Product;

public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    boolean existsBySkuValue(String sku);

    boolean existsByCompanyIdAndSkuValue(UUID companyId, String sku);

    Optional<Product> findBySkuValue(String sku);

    Optional<Product> findByIdAndCompanyId(UUID id, UUID companyId);

    Optional<Product> findByCompanyIdAndSkuValue(UUID companyId, String sku);

    List<Product> findAllByIdIn(Collection<UUID> ids);

    List<Product> findAllByCompanyIdAndIdIn(UUID companyId, Collection<UUID> ids);
}
