package com.thiagsilvadev.erpapi;

import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.BAD_REQUEST;
import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.CONFLICT;
import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.FORBIDDEN;
import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.NOT_FOUND;
import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.UNAUTHORIZED;
import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.UNPROCESSABLE_ENTITY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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
import com.thiagsilvadev.erpapi.catalog.internal.repository.SkuSequenceRepository;
import com.thiagsilvadev.erpapi.common.domain.numbering.BusinessNumberSequenceRepository;
import com.thiagsilvadev.erpapi.company.internal.repository.CompanyContactRepository;
import com.thiagsilvadev.erpapi.company.internal.repository.CompanyRepository;
import com.thiagsilvadev.erpapi.customer.internal.repository.ContactPersonRepository;
import com.thiagsilvadev.erpapi.customer.internal.repository.CustomerRepository;
import com.thiagsilvadev.erpapi.customer.internal.repository.SupplierRepository;
import com.thiagsilvadev.erpapi.financial.internal.repository.AccountingAccountRepository;
import com.thiagsilvadev.erpapi.financial.internal.repository.AccountingEntryRepository;
import com.thiagsilvadev.erpapi.financial.internal.repository.CashMovementRepository;
import com.thiagsilvadev.erpapi.financial.internal.repository.FinancialAccountRepository;
import com.thiagsilvadev.erpapi.financial.internal.repository.FinancialObligationRepository;
import com.thiagsilvadev.erpapi.financial.internal.repository.InvoiceRepository;
import com.thiagsilvadev.erpapi.financial.internal.repository.PaymentRepository;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.AuthorizationAuditRepository;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.PlatformRoleAssignmentRepository;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.RolePermissionRepository;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.TenantMembershipRepository;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.TenantRoleAssignmentRepository;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.TenantRoleRepository;
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
import com.thiagsilvadev.erpapi.user.internal.repository.UserRepository;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(
        properties = {
            "erp.iam.jwt.secret=test-secret-for-iam-jwt-signing-must-be-32-bytes",
            "springdoc.api-docs.enabled=true",
            "springdoc.swagger-ui.enabled=true"
        })
@EnableAutoConfiguration(
        exclude = {
            DataSourceAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class,
            DataJpaRepositoriesAutoConfiguration.class,
            EventPublicationAutoConfiguration.class
        },
        excludeName = JdbcEventPublicationAutoConfigurationExclusion.NAME)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ErpApiApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {}

    @Test
    void shouldExposeSwaggerUiAndConfigWithoutAuthenticationWhenEnabled() throws Exception {
        mockMvc.perform(get("/v3/api-docs/swagger-config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configUrl").value("/v3/api-docs/swagger-config"));

        mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
    }

    @Test
    void shouldExposeCurrentDriverShipmentInOpenApi() throws Exception {
        String path = "$['paths']['/api/v1/logistics/drivers/me/shipments/{id}']['get']";

        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(path + "['operationId']").value("getCurrentDriverShipment"))
                .andExpect(jsonPath(path + "['tags'][0]").value("driver-mobile"))
                .andExpect(jsonPath(path + "['responses']['200']").exists())
                .andExpect(jsonPath(path + "['responses']['401']").exists())
                .andExpect(jsonPath(path + "['responses']['403']").exists())
                .andExpect(jsonPath(path + "['responses']['404']").exists())
                .andExpect(jsonPath(path + "['responses']['200']['content']['application/json']['schema']['$ref']")
                        .value("#/components/schemas/ShipmentResponse"));
    }

    @Test
    void shouldPreserveOperationIdsForEveryHttpMethodAndPath() throws Exception {
        JsonNode expected;
        try (var resource = getClass().getResourceAsStream("/openapi-operation-ids.json")) {
            assertThat(resource).isNotNull();
            expected = JsonMapper.builder().build().readTree(resource);
        }
        Map<String, String> expectedIds = new HashMap<>();
        expected.properties()
                .forEach(entry ->
                        expectedIds.put(entry.getKey(), entry.getValue().asText()));
        Map<String, String> actualIds = new HashMap<>();
        for (var path : openApi().path("paths").properties()) {
            if (!path.getKey().startsWith("/api/v1/")) {
                continue;
            }
            for (var method : path.getValue().properties()) {
                if (Set.of("get", "post", "put", "patch", "delete").contains(method.getKey())) {
                    actualIds.put(
                            method.getKey().toUpperCase(Locale.ROOT) + " " + path.getKey(),
                            method.getValue().path("operationId").asText());
                }
            }
        }
        assertThat(expectedIds).hasSize(97);
        assertThat(actualIds).containsExactlyInAnyOrderEntriesOf(expectedIds);
    }

    @Test
    void shouldDescribeEveryBusinessOperationWithReusableErrorsAndSecurity() throws Exception {
        JsonNode document = openApi();
        Map<String, String> errorRefs = Map.of(
                "400",
                BAD_REQUEST,
                "401",
                UNAUTHORIZED,
                "403",
                FORBIDDEN,
                "404",
                NOT_FOUND,
                "409",
                CONFLICT,
                "422",
                UNPROCESSABLE_ENTITY);
        Set<String> operationIds = new HashSet<>();
        Set<String> declaredTags = new HashSet<>();
        Set<String> usedTags = new HashSet<>();
        document.path("tags").forEach(tag -> {
            String tagName = tag.path("name").asText();
            assertThat(tagName).matches("^[a-z0-9]+(-[a-z0-9]+)*$");
            assertThat(tag.path("description").asText()).isNotBlank();
            assertThat(declaredTags.add(tagName)).isTrue();
        });
        int operations = 0;
        for (var path : document.path("paths").properties()) {
            if (!path.getKey().startsWith("/api/v1/")) {
                continue;
            }
            for (var entry : path.getValue().properties()) {
                if (!Set.of("get", "post", "put", "patch", "delete").contains(entry.getKey())) {
                    continue;
                }
                operations++;
                JsonNode operation = entry.getValue();
                String id = operation.path("operationId").asText();
                assertThat(id).isNotBlank();
                assertThat(operationIds.add(id))
                        .as("Unique operation ID: %s", id)
                        .isTrue();
                operation.path("tags").forEach(tag -> usedTags.add(tag.asText()));
                boolean login = path.getKey().equals("/api/v1/auth/login");
                if (login) {
                    assertThat(operation.path("security").isMissingNode()
                                    || operation.path("security").isEmpty())
                            .isTrue();
                } else {
                    assertThat(operation.at("/security/0/bearerAuth").isArray())
                            .as("Bearer requirement for %s", id)
                            .isTrue();
                    assertThat(operation.at("/responses/401/$ref").asText()).isEqualTo(UNAUTHORIZED);
                    assertThat(operation.at("/responses/403/$ref").asText()).isEqualTo(FORBIDDEN);
                }
                boolean success = false;
                for (var response : operation.path("responses").properties()) {
                    String status = response.getKey();
                    if (status.startsWith("2")) {
                        success = true;
                        assertThat(response.getValue().path("description").asText())
                                .isNotBlank();
                    }
                    if (errorRefs.containsKey(status)) {
                        assertThat(response.getValue().path("$ref").asText())
                                .as("Reusable %s response for %s", status, id)
                                .isEqualTo(errorRefs.get(status));
                    }
                    if (status.equals("204")) {
                        assertThat(response.getValue().has("content")).isFalse();
                    }
                }
                assertThat(success).as("Success response for %s", id).isTrue();
            }
        }
        assertThat(operations).isEqualTo(97);
        assertThat(usedTags).containsExactlyInAnyOrderElementsOf(declaredTags);
        for (String ref : errorRefs.values()) {
            JsonNode response = document.at(ref.substring(1));
            assertThat(response.isMissingNode())
                    .as("Resolvable response %s", ref)
                    .isFalse();
            assertThat(response.path("description").asText()).isNotBlank();
            assertThat(response.at("/content/application~1problem+json/schema/$ref")
                            .asText())
                    .isEqualTo("#/components/schemas/ProblemDetail");
            assertThat(response.path("content").size()).isEqualTo(1);
        }
        JsonNode problem = document.at("/components/schemas/ProblemDetail");
        assertThat(problem.at("/properties/error_code/type").asText()).isEqualTo("string");
        JsonNode invalidParam = problem.at("/properties/invalid_params/items/properties");
        assertThat(invalidParam.path("name").path("type").asText()).isEqualTo("string");
        assertThat(invalidParam.path("reason").path("type").asText()).isEqualTo("string");
        assertThat(problem.path("required").isMissingNode()
                        || problem.path("required").isEmpty())
                .isTrue();
        assertThat(problem.path("additionalProperties").asBoolean()).isTrue();
    }

    @Test
    void shouldPreservePayloadSchemasAndDocumentApplicableFailures() throws Exception {
        JsonNode document = openApi();
        JsonNode product = operation(document, "/api/v1/catalog/products", "post");
        assertThat(product.at("/responses/201/content/application~1json/schema/$ref")
                        .asText())
                .isEqualTo("#/components/schemas/ProductResponse");
        assertThat(product.at("/responses/404/$ref").asText()).isEqualTo(NOT_FOUND);
        assertThat(product.at("/responses/409/$ref").asText()).isEqualTo(CONFLICT);
        assertThat(product.at("/responses/422/$ref").asText()).isEqualTo(UNPROCESSABLE_ENTITY);

        JsonNode roleList = operation(document, "/api/v1/iam/roles", "get");
        assertThat(roleList.at("/responses/200/content/application~1json/schema/type")
                        .asText())
                .isEqualTo("array");
        assertThat(roleList.at("/responses/200/content/application~1json/schema/items/$ref")
                        .asText())
                .isEqualTo("#/components/schemas/TenantRoleResponse");
        assertThat(roleList.path("responses").has("404")).isFalse();
        assertThat(roleList.path("responses").has("409")).isFalse();
        assertThat(roleList.path("responses").has("422")).isFalse();

        JsonNode products = operation(document, "/api/v1/catalog/products", "get");
        String pageRef = products.at("/responses/200/content/application~1json/schema/$ref")
                .asText();
        assertThat(pageRef).startsWith("#/components/schemas/");
        JsonNode page = document.at(pageRef.substring(1));
        assertThat(page.at("/properties/content/type").asText()).isEqualTo("array");
        assertThat(page.at("/properties/content/items/$ref").asText())
                .isEqualTo("#/components/schemas/ProductResponse");
        Set<String> queryParameters = queryParameters(products);
        assertThat(queryParameters).contains("page", "size", "sort", "name", "sku");

        assertThat(queryParameters(operation(document, "/api/v1/kpis/executive", "get")))
                .contains("from", "to", "bucket")
                .doesNotContain("complete", "ordered", "withinMaximumRange");
        assertThat(queryParameters(operation(document, "/api/v1/financial/accounting-entries", "get")))
                .contains("accountId", "from", "to", "page", "size", "sort")
                .doesNotContain("completeRange", "orderedRange");
        assertThat(queryParameters(operation(document, "/api/v1/financial/accounting-entries/balances", "get")))
                .contains("from", "to", "currencyCode")
                .doesNotContain("orderedRange");

        assertThat(document.at("/components/schemas/MoneyRequest/properties/currencyCode/description")
                        .asText())
                .contains("ISO 4217");
        assertThat(document.at("/components/schemas/MoneyRequest/properties/currencyCode/example")
                        .asText())
                .isEqualTo("BRL");
        assertThat(document.at("/components/schemas/CreateCompanyRequest/properties/cnpj/description")
                        .asText())
                .contains("14-digit Brazilian CNPJ");
        assertThat(document.at("/components/schemas/CreateCompanyRequest/properties/cnpj/example")
                        .asText())
                .isEqualTo("11222333000181");
        assertThat(document.at("/components/schemas/CreateVehicleRequest/properties/plate/description")
                        .asText())
                .contains("Mercosul vehicle license plate");
        assertThat(document.at("/components/schemas/CreateVehicleRequest/properties/plate/example")
                        .asText())
                .isEqualTo("ABC1D23");
        JsonNode kpiExecutive = operation(document, "/api/v1/kpis/executive", "get");
        Map<String, String> kpiParamDescriptions = new HashMap<>();
        kpiExecutive
                .path("parameters")
                .forEach(p -> kpiParamDescriptions.put(
                        p.path("name").asText(), p.path("description").asText()));
        assertThat(kpiParamDescriptions.get("from")).contains("Inclusive UTC date");
        assertThat(kpiParamDescriptions.get("bucket")).contains("defaults to DAY when omitted");

        JsonNode companyDelete = operation(document, "/api/v1/platform/companies/{id}", "delete");
        assertThat(companyDelete.at("/responses/204").has("content")).isFalse();
        JsonNode roleDelete = operation(document, "/api/v1/iam/roles/{roleId}", "delete");
        assertThat(roleDelete.at("/responses/204").has("content")).isFalse();
    }

    @Test
    void shouldDescribeTokenSelectionWithoutExposingAuthenticationPrincipals() throws Exception {
        JsonNode document = openApi();
        JsonNode platform = operation(document, "/api/v1/auth/select-platform", "post");
        assertThat(platform.path("operationId").asText()).isEqualTo("selectPlatform");
        assertThat(platform.path("description").asText()).contains("token_use=global");
        assertThat(platform.has("parameters")).isFalse();
        assertThat(platform.has("requestBody")).isFalse();
        JsonNode company = operation(document, "/api/v1/auth/select-company", "post");
        assertThat(company.has("parameters")).isFalse();
        assertThat(company.path("requestBody").path("required").asBoolean()).isTrue();
        JsonNode driver = operation(document, "/api/v1/logistics/drivers/me/shipments/{id}", "get");
        assertThat(driver.path("parameters").size()).isEqualTo(1);
        assertThat(driver.at("/parameters/0/name").asText()).isEqualTo("id");
        assertThat(driver.at("/parameters/0/in").asText()).isEqualTo("path");
        String scheme = document.at("/components/securitySchemes/bearerAuth/description")
                .asText();
        assertThat(scheme).contains("token_use=global", "token_use=scoped", "token_use=platform");
    }

    private JsonNode openApi() throws Exception {
        String json = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return JsonMapper.builder().build().readTree(json);
    }

    private JsonNode operation(JsonNode document, String path, String verb) {
        JsonNode operation = document.path("paths").path(path).path(verb);
        assertThat(operation.isMissingNode()).as("Documented %s %s", verb, path).isFalse();
        return operation;
    }

    private Set<String> queryParameters(JsonNode operation) {
        Set<String> parameters = new HashSet<>();
        operation.path("parameters").forEach(parameter -> {
            assertThat(parameter.path("in").asText()).isEqualTo("query");
            parameters.add(parameter.path("name").asText());
        });
        return parameters;
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
        SkuSequenceRepository skuSequenceRepository() {
            return mock(SkuSequenceRepository.class);
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
        TenantMembershipRepository tenantMembershipRepository() {
            return mock(TenantMembershipRepository.class);
        }

        @Bean
        TenantRoleRepository tenantRoleRepository() {
            return mock(TenantRoleRepository.class);
        }

        @Bean
        RolePermissionRepository rolePermissionRepository() {
            return mock(RolePermissionRepository.class);
        }

        @Bean
        TenantRoleAssignmentRepository tenantRoleAssignmentRepository() {
            return mock(TenantRoleAssignmentRepository.class);
        }

        @Bean
        PlatformRoleAssignmentRepository platformRoleAssignmentRepository() {
            return mock(PlatformRoleAssignmentRepository.class);
        }

        @Bean
        AuthorizationAuditRepository authorizationAuditRepository() {
            return mock(AuthorizationAuditRepository.class);
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
        FinancialObligationRepository financialObligationRepository() {
            return mock(FinancialObligationRepository.class);
        }

        @Bean
        FinancialAccountRepository financialAccountRepository() {
            return mock(FinancialAccountRepository.class);
        }

        @Bean
        CashMovementRepository cashMovementRepository() {
            return mock(CashMovementRepository.class);
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

final class JdbcEventPublicationAutoConfigurationExclusion {

    static final String NAME =
            "org." + "springframework.modulith.events.jdbc." + "JdbcEventPublicationAutoConfiguration";

    private JdbcEventPublicationAutoConfigurationExclusion() {}
}
