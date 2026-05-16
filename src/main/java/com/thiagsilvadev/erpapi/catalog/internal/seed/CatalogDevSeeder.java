package com.thiagsilvadev.erpapi.catalog.internal.seed;

import java.math.BigDecimal;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.catalog.internal.model.Category;
import com.thiagsilvadev.erpapi.catalog.internal.model.Product;
import com.thiagsilvadev.erpapi.catalog.internal.repository.CategoryRepository;
import com.thiagsilvadev.erpapi.catalog.internal.repository.ProductRepository;
import com.thiagsilvadev.erpapi.common.domain.model.Money;

@Component
@Profile("dev")
@Order(200)
class CatalogDevSeeder implements ApplicationRunner {

    static final String INDUSTRIAL_CATEGORY_CODE = "INDUSTRIAL";
    static final String INDUSTRIAL_WIDGET_SKU = "SKU-001";

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    CatalogDevSeeder(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Category category = categoryRepository
                .findByCode(Category.normalizeCode(INDUSTRIAL_CATEGORY_CODE))
                .orElseGet(this::seedCategory);

        if (!productRepository.existsBySku(Product.normalizeSku(INDUSTRIAL_WIDGET_SKU))) {
            productRepository.save(Product.create(
                    INDUSTRIAL_WIDGET_SKU,
                    "Industrial Widget",
                    "Standard catalog product",
                    Money.reais(new BigDecimal("10.50")),
                    category));
        }
    }

    private Category seedCategory() {
        return categoryRepository.save(
                Category.create(INDUSTRIAL_CATEGORY_CODE, "Industrial Supplies", "Seeded industrial catalog category"));
    }
}
