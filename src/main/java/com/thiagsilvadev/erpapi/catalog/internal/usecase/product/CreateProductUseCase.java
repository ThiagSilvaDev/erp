package com.thiagsilvadev.erpapi.catalog.internal.usecase.product;

import static com.thiagsilvadev.erpapi.catalog.api.CatalogPermissions.WRITE;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.catalog.internal.dto.product.CreateProductRequest;
import com.thiagsilvadev.erpapi.catalog.internal.dto.product.ProductResponse;
import com.thiagsilvadev.erpapi.catalog.internal.event.CatalogEventPublisher;
import com.thiagsilvadev.erpapi.catalog.internal.exception.SkuAlreadyExistsException;
import com.thiagsilvadev.erpapi.catalog.internal.mapper.CatalogMapper;
import com.thiagsilvadev.erpapi.catalog.internal.model.Category;
import com.thiagsilvadev.erpapi.catalog.internal.model.Product;
import com.thiagsilvadev.erpapi.catalog.internal.model.Sku;
import com.thiagsilvadev.erpapi.catalog.internal.repository.CategoryRepository;
import com.thiagsilvadev.erpapi.catalog.internal.repository.ProductRepository;
import com.thiagsilvadev.erpapi.catalog.internal.service.SkuAssignmentService;
import com.thiagsilvadev.erpapi.catalog.internal.service.SkuGenerationContext;
import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.domain.model.Money;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;

@Component
public class CreateProductUseCase {

    private final SkuAssignmentService skuAssignmentService;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CatalogMapper catalogMapper;
    private final CatalogEventPublisher eventPublisher;

    public CreateProductUseCase(
            ProductRepository productRepository,
            SkuAssignmentService skuAssignmentService,
            CategoryRepository categoryRepository,
            CatalogMapper catalogMapper,
            CatalogEventPublisher eventPublisher) {
        this.productRepository = productRepository;
        this.skuAssignmentService = skuAssignmentService;
        this.categoryRepository = categoryRepository;
        this.catalogMapper = catalogMapper;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    @PreAuthorize("hasAuthority('" + WRITE + "')")
    public ProductResponse execute(CreateProductRequest request) {
        var companyId = TenantContext.requireTenantId();
        Category category = categoryRepository
                .findByIdAndCompanyId(request.categoryId(), companyId)
                .orElseThrow(() -> new ResourceNotFoundException(Category.class, request.categoryId()));
        Money basePrice =
                Money.of(request.basePrice().amount(), request.basePrice().currencyCode());
        Sku sku = skuAssignmentService.assign(request.sku(), new SkuGenerationContext(companyId, category.getCode()));
        Product product = Product.create(companyId, sku, request.name(), request.description(), basePrice, category);

        Product savedProduct;
        try {
            savedProduct = productRepository.saveAndFlush(product);
        } catch (DataIntegrityViolationException e) {
            Throwable cause = e;
            while (cause != null) {
                if (cause instanceof ConstraintViolationException violation
                        && "uk_catalog_product_company_sku".equals(violation.getConstraintName())) {
                    throw new SkuAlreadyExistsException(sku.getValue());
                }
                cause = cause.getCause();
            }
            throw e;
        }
        eventPublisher.productCreated(savedProduct);

        return catalogMapper.toResponse(savedProduct);
    }
}
