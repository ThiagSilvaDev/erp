package com.thiagsilvadev.erpapi.catalog.internal.seed;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.catalog.internal.model.Category;
import com.thiagsilvadev.erpapi.catalog.internal.model.Product;
import com.thiagsilvadev.erpapi.catalog.internal.model.Sku;
import com.thiagsilvadev.erpapi.catalog.internal.repository.CategoryRepository;
import com.thiagsilvadev.erpapi.catalog.internal.repository.ProductRepository;
import com.thiagsilvadev.erpapi.common.domain.model.Money;
import com.thiagsilvadev.erpapi.company.api.CompanyFacade;

@Component
@Profile("dev")
@Order(200)
class CatalogDevSeeder implements ApplicationRunner {

    static final String INDUSTRIAL_CATEGORY_CODE = "INDUSTRIAL";
    static final String OFFICE_CATEGORY_CODE = "OFFICE";
    static final String INDUSTRIAL_WIDGET_SKU = "SKU-001";
    private static final String ACME_CNPJ = "97.986.234/0001-91";

    private static final List<CategorySeed> CATEGORIES = List.of(
            new CategorySeed(INDUSTRIAL_CATEGORY_CODE, "Industrial Supplies", "Seeded industrial catalog category"),
            new CategorySeed(OFFICE_CATEGORY_CODE, "Office Supplies", "Seeded office catalog category"));

    private static final List<ProductSeed> PRODUCTS = List.of(
            new ProductSeed(
                    INDUSTRIAL_WIDGET_SKU,
                    "Industrial Widget",
                    "Standard catalog product",
                    "10.50",
                    INDUSTRIAL_CATEGORY_CODE),
            new ProductSeed(
                    "SKU-002", "Hydraulic Pump", "High-pressure hydraulic pump", "320.00", INDUSTRIAL_CATEGORY_CODE),
            new ProductSeed("SKU-003", "Bearing Kit", "Replacement bearing kit", "48.90", INDUSTRIAL_CATEGORY_CODE),
            new ProductSeed(
                    "SKU-004", "Safety Gloves", "Cut-resistant safety gloves", "18.75", INDUSTRIAL_CATEGORY_CODE),
            new ProductSeed(
                    "SKU-005",
                    "Industrial Sealant",
                    "Multi-purpose industrial sealant",
                    "26.40",
                    INDUSTRIAL_CATEGORY_CODE),
            new ProductSeed(
                    "SKU-006", "Steel Fastener Pack", "Assorted steel fasteners", "14.20", INDUSTRIAL_CATEGORY_CODE),
            new ProductSeed("SKU-007", "Copy Paper", "A4 white copy paper ream", "7.95", OFFICE_CATEGORY_CODE),
            new ProductSeed("SKU-008", "Ballpoint Pen Box", "Blue ballpoint pens", "12.30", OFFICE_CATEGORY_CODE),
            new ProductSeed("SKU-009", "Desk Organizer", "Compact desktop organizer", "22.10", OFFICE_CATEGORY_CODE),
            new ProductSeed(
                    "SKU-010", "Shipping Labels", "Self-adhesive shipping labels", "16.80", OFFICE_CATEGORY_CODE));

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CompanyFacade companyFacade;

    CatalogDevSeeder(
            CategoryRepository categoryRepository, ProductRepository productRepository, CompanyFacade companyFacade) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.companyFacade = companyFacade;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        var companyId = companyFacade
                .findByCnpj(ACME_CNPJ)
                .orElseThrow(() -> new IllegalStateException("Seed company must exist before seeding catalog"))
                .id();
        Map<String, Category> categories = CATEGORIES.stream()
                .map(category -> seedCategory(companyId, category))
                .collect(Collectors.toMap(Category::getCode, Function.identity()));

        for (ProductSeed product : PRODUCTS) {
            if (!productRepository.existsByCompanyIdAndSkuValue(
                    companyId, Sku.of(product.sku()).getValue())) {
                productRepository.save(Product.create(
                        companyId,
                        product.sku(),
                        product.name(),
                        product.description(),
                        Money.reais(new BigDecimal(product.basePrice())),
                        categories.get(Category.normalizeCode(product.categoryCode()))));
            }
        }
    }

    private Category seedCategory(java.util.UUID companyId, CategorySeed category) {
        return categoryRepository
                .findByCompanyIdAndCode(companyId, Category.normalizeCode(category.code()))
                .orElseGet(() -> categoryRepository.save(
                        Category.create(companyId, category.code(), category.name(), category.description())));
    }

    private record CategorySeed(String code, String name, String description) {}

    private record ProductSeed(String sku, String name, String description, String basePrice, String categoryCode) {}
}
