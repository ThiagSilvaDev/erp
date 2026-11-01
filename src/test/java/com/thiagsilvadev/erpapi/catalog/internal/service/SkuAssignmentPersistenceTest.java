package com.thiagsilvadev.erpapi.catalog.internal.service;

import static com.thiagsilvadev.erpapi.catalog.api.CatalogPermissions.WRITE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.thiagsilvadev.erpapi.catalog.internal.dto.MoneyRequest;
import com.thiagsilvadev.erpapi.catalog.internal.dto.product.CreateProductRequest;
import com.thiagsilvadev.erpapi.catalog.internal.exception.SkuAlreadyExistsException;
import com.thiagsilvadev.erpapi.catalog.internal.model.Category;
import com.thiagsilvadev.erpapi.catalog.internal.model.Product;
import com.thiagsilvadev.erpapi.catalog.internal.repository.CategoryRepository;
import com.thiagsilvadev.erpapi.catalog.internal.repository.ProductRepository;
import com.thiagsilvadev.erpapi.catalog.internal.repository.SkuSequenceRepository;
import com.thiagsilvadev.erpapi.catalog.internal.usecase.product.CreateProductUseCase;
import com.thiagsilvadev.erpapi.common.domain.model.Money;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;

@SpringBootTest
@ActiveProfiles("dbtest")
class SkuAssignmentPersistenceTest {
    @Autowired
    CreateProductUseCase useCase;

    @Autowired
    SkuAssignmentService assignment;

    @Autowired
    ProductRepository products;

    @Autowired
    CategoryRepository categories;

    @Autowired
    SkuSequenceRepository sequences;

    @Autowired
    PlatformTransactionManager transactions;

    private final UUID company = UUID.randomUUID();
    private final UUID other = UUID.randomUUID();

    @AfterEach
    void cleanup() {
        tx().executeWithoutResult(status -> {
            products.findAll().stream()
                    .filter(p ->
                            p.getCompanyId().equals(company) || p.getCompanyId().equals(other))
                    .forEach(products::delete);
            products.flush();
            categories.findAll().stream()
                    .filter(c ->
                            c.getCompanyId().equals(company) || c.getCompanyId().equals(other))
                    .forEach(categories::delete);
            sequences.findById(company).ifPresent(sequences::delete);
            sequences.findById(other).ifPresent(sequences::delete);
        });
    }

    @Test
    void shouldShareCounterAcrossCategoriesAndIsolateCompanies() {
        Category office = category(company, "OFFICE");
        Category tools = category(company, "TOOLS");
        Category otherOffice = category(other, "OFFICE");
        assertThat(create(office, null)).isEqualTo("OFFICE-00000001");
        assertThat(create(tools, null)).isEqualTo("TOOLS-00000002");
        assertThat(create(otherOffice, null)).isEqualTo("OFFICE-00000001");
    }

    @Test
    void shouldSkipManualCodesWithoutAdvancingForManualCreation() {
        Category office = category(company, "OFFICE");
        assertThat(create(office, " office-00000001 ")).isEqualTo("OFFICE-00000001");
        assertThat(create(office, null)).isEqualTo("OFFICE-00000002");
        assertThatThrownBy(() -> create(office, "OFFICE-00000001")).isInstanceOf(SkuAlreadyExistsException.class);
    }

    @Test
    void shouldRollBackProductAndAllocation() {
        Category office = category(company, "OFFICE");
        tx().executeWithoutResult(status -> {
            persist(office, null);
            status.setRollbackOnly();
        });
        assertThat(products.findByCompanyIdAndSkuValue(company, "OFFICE-00000001"))
                .isEmpty();
        assertThat(create(office, null)).isEqualTo("OFFICE-00000001");
    }

    @Test
    void shouldSerializeConcurrentFirstUse() throws Exception {
        Category office = category(company, "OFFICE");
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> {
                ready.countDown();
                start.await();
                return create(office, null);
            });
            var second = executor.submit(() -> {
                ready.countDown();
                start.await();
                return create(office, null);
            });
            try {
                assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            } finally {
                start.countDown();
            }
            assertThat(List.of(first.get(15, TimeUnit.SECONDS), second.get(15, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder("OFFICE-00000001", "OFFICE-00000002");
        }
    }

    @Test
    @WithMockUser(authorities = WRITE)
    void shouldCreateThroughUseCaseAndRollBackAllocationOnFailure() {
        Category office = category(company, "OFFICE");
        TenantContext.setTenantId(company);
        try {
            var request = new CreateProductRequest(
                    null, "Test", null, new MoneyRequest(BigDecimal.TEN, "BRL"), office.getId());
            tx().executeWithoutResult(status -> {
                useCase.execute(request);
                status.setRollbackOnly();
            });
            assertThat(useCase.execute(request).sku()).isEqualTo("OFFICE-00000001");
            var manual = new CreateProductRequest(
                    " custom ", "Test", null, new MoneyRequest(BigDecimal.TEN, "BRL"), office.getId());
            assertThat(useCase.execute(manual).sku()).isEqualTo("CUSTOM");
        } finally {
            TenantContext.clear();
        }
    }

    private Category category(UUID tenant, String code) {
        return tx().execute(status -> categories.saveAndFlush(Category.create(tenant, code, code, null)));
    }

    private String create(Category category, String supplied) {
        return tx().execute(status -> persist(category, supplied));
    }

    private String persist(Category category, String supplied) {
        var sku = assignment.assign(supplied, new SkuGenerationContext(category.getCompanyId(), category.getCode()));
        var product =
                Product.create(category.getCompanyId(), sku, "Test", null, Money.of(BigDecimal.TEN, "BRL"), category);
        return products.saveAndFlush(product).getSku();
    }

    private TransactionTemplate tx() {
        return new TransactionTemplate(transactions);
    }
}
