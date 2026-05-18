package com.thiagsilvadev.erpapi;

import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

import com.thiagsilvadev.erpapi.catalog.internal.repository.CategoryRepository;
import com.thiagsilvadev.erpapi.catalog.internal.repository.ProductRepository;
import com.thiagsilvadev.erpapi.common.domain.numbering.BusinessNumberSequenceRepository;
import com.thiagsilvadev.erpapi.company.internal.repository.CompanyRepository;
import com.thiagsilvadev.erpapi.customer.internal.repository.ContactPersonRepository;
import com.thiagsilvadev.erpapi.customer.internal.repository.CustomerRepository;
import com.thiagsilvadev.erpapi.customer.internal.repository.SupplierRepository;
import com.thiagsilvadev.erpapi.financial.internal.repository.InvoiceRepository;
import com.thiagsilvadev.erpapi.financial.internal.repository.LedgerEntryRepository;
import com.thiagsilvadev.erpapi.financial.internal.repository.PaymentRepository;
import com.thiagsilvadev.erpapi.iam.internal.repository.IamCredentialRepository;
import com.thiagsilvadev.erpapi.inventory.internal.repository.InventoryItemRepository;
import com.thiagsilvadev.erpapi.inventory.internal.repository.InventoryReservationRepository;
import com.thiagsilvadev.erpapi.inventory.internal.repository.StockMovementRepository;
import com.thiagsilvadev.erpapi.inventory.internal.repository.WarehouseRepository;
import com.thiagsilvadev.erpapi.sales.internal.repository.SalesOrderRepository;
import com.thiagsilvadev.erpapi.user.internal.repository.UserRepository;

@SpringBootTest(
        properties = {
            "spring.autoconfigure.exclude="
                    + "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,"
                    + "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration,"
                    + "org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration,"
                    + "org.springframework.modulith.events.config.EventPublicationAutoConfiguration,"
                    + "org.springframework.modulith.events.jpa.JpaEventPublicationAutoConfiguration",
            "erp.iam.jwt.secret=test-secret-for-iam-jwt-signing-must-be-32-bytes"
        })
@ActiveProfiles("test")
class ErpApiApplicationTests {

    @Test
    void contextLoads() {}

    @TestConfiguration
    static class CompanyRepositoryTestConfiguration {

        @Bean
        CompanyRepository companyRepository() {
            return mock(CompanyRepository.class);
        }

        @Bean
        CategoryRepository categoryRepository() {
            return mock(CategoryRepository.class);
        }

        @Bean
        ProductRepository productRepository() {
            return mock(ProductRepository.class);
        }

        @Bean
        UserRepository userRepository() {
            return mock(UserRepository.class);
        }

        @Bean
        IamCredentialRepository iamCredentialRepository() {
            return mock(IamCredentialRepository.class);
        }

        @Bean
        CustomerRepository customerRepository() {
            return mock(CustomerRepository.class);
        }

        @Bean
        SupplierRepository supplierRepository() {
            return mock(SupplierRepository.class);
        }

        @Bean
        ContactPersonRepository contactPersonRepository() {
            return mock(ContactPersonRepository.class);
        }

        @Bean
        WarehouseRepository warehouseRepository() {
            return mock(WarehouseRepository.class);
        }

        @Bean
        InventoryItemRepository inventoryItemRepository() {
            return mock(InventoryItemRepository.class);
        }

        @Bean
        StockMovementRepository stockMovementRepository() {
            return mock(StockMovementRepository.class);
        }

        @Bean
        InventoryReservationRepository inventoryReservationRepository() {
            return mock(InventoryReservationRepository.class);
        }

        @Bean
        SalesOrderRepository salesOrderRepository() {
            return mock(SalesOrderRepository.class);
        }

        @Bean
        InvoiceRepository invoiceRepository() {
            return mock(InvoiceRepository.class);
        }

        @Bean
        PaymentRepository paymentRepository() {
            return mock(PaymentRepository.class);
        }

        @Bean
        LedgerEntryRepository ledgerEntryRepository() {
            return mock(LedgerEntryRepository.class);
        }

        @Bean
        BusinessNumberSequenceRepository businessNumberSequenceRepository() {
            return mock(BusinessNumberSequenceRepository.class);
        }
    }
}
