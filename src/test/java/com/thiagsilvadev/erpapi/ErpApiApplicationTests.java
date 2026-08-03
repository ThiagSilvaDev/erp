package com.thiagsilvadev.erpapi;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.modulith.events.config.EventPublicationAutoConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.thiagsilvadev.erpapi.catalog.internal.repository.CategoryRepository;
import com.thiagsilvadev.erpapi.catalog.internal.repository.ProductRepository;
import com.thiagsilvadev.erpapi.common.domain.numbering.BusinessNumberSequenceRepository;
import com.thiagsilvadev.erpapi.company.internal.repository.CompanyContactRepository;
import com.thiagsilvadev.erpapi.company.internal.repository.CompanyRepository;
import com.thiagsilvadev.erpapi.customer.internal.repository.ContactPersonRepository;
import com.thiagsilvadev.erpapi.customer.internal.repository.CustomerRepository;
import com.thiagsilvadev.erpapi.customer.internal.repository.SupplierRepository;
import com.thiagsilvadev.erpapi.financial.internal.repository.AccountingAccountRepository;
import com.thiagsilvadev.erpapi.financial.internal.repository.AccountingEntryRepository;
import com.thiagsilvadev.erpapi.financial.internal.repository.FinancialAccountRepository;
import com.thiagsilvadev.erpapi.financial.internal.repository.FinancialEntryRepository;
import com.thiagsilvadev.erpapi.financial.internal.repository.InvoiceRepository;
import com.thiagsilvadev.erpapi.financial.internal.repository.LedgerEntryRepository;
import com.thiagsilvadev.erpapi.financial.internal.repository.PaymentRepository;
import com.thiagsilvadev.erpapi.iam.internal.repository.IamCredentialRepository;
import com.thiagsilvadev.erpapi.inventory.internal.repository.InventoryItemRepository;
import com.thiagsilvadev.erpapi.inventory.internal.repository.InventoryReservationRepository;
import com.thiagsilvadev.erpapi.inventory.internal.repository.StockMovementRepository;
import com.thiagsilvadev.erpapi.inventory.internal.repository.WarehouseRepository;
import com.thiagsilvadev.erpapi.logistics.internal.repository.DriverRepository;
import com.thiagsilvadev.erpapi.logistics.internal.repository.ShipmentRepository;
import com.thiagsilvadev.erpapi.logistics.internal.repository.ShipmentTrackingEventRepository;
import com.thiagsilvadev.erpapi.logistics.internal.repository.VehicleRepository;
import com.thiagsilvadev.erpapi.sales.internal.repository.SalesOrderRepository;
import com.thiagsilvadev.erpapi.user.internal.repository.UserMembershipRepository;
import com.thiagsilvadev.erpapi.user.internal.repository.UserRepository;

@SpringBootTest(properties = {"erp.iam.jwt.secret=test-secret-for-iam-jwt-signing-must-be-32-bytes"})
@EnableAutoConfiguration(
        exclude = {
            DataSourceAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class,
            DataJpaRepositoriesAutoConfiguration.class,
            EventPublicationAutoConfiguration.class
        },
        excludeName = JpaEventPublicationAutoConfigurationExclusion.NAME)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ErpApiApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {}

    @Test
    void shouldExposeCurrentDriverShipmentInOpenApi() throws Exception {
        String path = "$['paths']['/api/v1/logistics/drivers/me/shipments/{id}']['get']";

        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(path + "['operationId']").value("getCurrentDriverShipment"))
                .andExpect(jsonPath(path + "['tags'][0]").value("driver mobile"))
                .andExpect(jsonPath(path + "['responses']['200']").exists())
                .andExpect(jsonPath(path + "['responses']['401']").exists())
                .andExpect(jsonPath(path + "['responses']['403']").exists())
                .andExpect(jsonPath(path + "['responses']['404']").exists())
                .andExpect(jsonPath(path + "['responses']['200']['content']['application/json']['schema']['$ref']")
                        .value("#/components/schemas/ShipmentResponse"));
    }

    @TestConfiguration
    static class CompanyRepositoryTestConfiguration {

        @Bean
        CompanyRepository companyRepository() {
            return mock(CompanyRepository.class);
        }

        @Bean
        CompanyContactRepository companyContactRepository() {
            return mock(CompanyContactRepository.class);
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
        UserMembershipRepository userMembershipRepository() {
            return mock(UserMembershipRepository.class);
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
        FinancialAccountRepository financialAccountRepository() {
            return mock(FinancialAccountRepository.class);
        }

        @Bean
        FinancialEntryRepository financialEntryRepository() {
            return mock(FinancialEntryRepository.class);
        }

        @Bean
        AccountingAccountRepository accountingAccountRepository() {
            return mock(AccountingAccountRepository.class);
        }

        @Bean
        AccountingEntryRepository accountingEntryRepository() {
            return mock(AccountingEntryRepository.class);
        }

        @Bean
        BusinessNumberSequenceRepository businessNumberSequenceRepository() {
            return mock(BusinessNumberSequenceRepository.class);
        }

        @Bean
        DriverRepository driverRepository() {
            return mock(DriverRepository.class);
        }

        @Bean
        VehicleRepository vehicleRepository() {
            return mock(VehicleRepository.class);
        }

        @Bean
        ShipmentRepository shipmentRepository() {
            return mock(ShipmentRepository.class);
        }

        @Bean
        ShipmentTrackingEventRepository shipmentTrackingEventRepository() {
            return mock(ShipmentTrackingEventRepository.class);
        }
    }
}

final class JpaEventPublicationAutoConfigurationExclusion {

    static final String NAME = "org." + "springframework.modulith.events.jpa." + "JpaEventPublicationAutoConfiguration";

    private JpaEventPublicationAutoConfigurationExclusion() {}
}
