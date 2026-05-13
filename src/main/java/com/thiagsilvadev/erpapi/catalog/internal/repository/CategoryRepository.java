package com.thiagsilvadev.erpapi.catalog.internal.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.thiagsilvadev.erpapi.catalog.internal.model.Category;

public interface CategoryRepository extends JpaRepository<Category, UUID>, JpaSpecificationExecutor<Category> {

    boolean existsByCode(String code);

    Optional<Category> findByCode(String code);
}
