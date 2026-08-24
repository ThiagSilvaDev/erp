package com.thiagsilvadev.erpapi.iam.internal.config;

import static com.thiagsilvadev.erpapi.catalog.api.CatalogPermissions.WRITE;
import static com.thiagsilvadev.erpapi.iam.accesscontrol.api.AuthorizationStatus.ACTIVE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.modulith.events.config.EventPublicationAutoConfiguration;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.common.web.exception.ProblemDetailFactory;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.AccessControlFacade;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.TenantAuthorizationSnapshot;
import com.thiagsilvadev.erpapi.iam.internal.security.CustomAccessDeniedHandler;
import com.thiagsilvadev.erpapi.iam.internal.security.CustomAuthenticationEntryPoint;

@SpringBootTest(
        classes = {
            IamSecurityConfigurationTest.TestApplication.class,
            IamSecurityConfigurationTest.DummyController.class,
            IamSecurityConfigurationTest.PermissionController.class,
            IamSecurityConfigurationTest.TestWriteUseCase.class,
            IamSecurityConfiguration.class,
            ProblemDetailFactory.class,
            CustomAuthenticationEntryPoint.class,
            CustomAccessDeniedHandler.class
        },
        properties = {
            "erp.iam.jwt.secret=test-secret-for-iam-jwt-signing-must-be-32-bytes",
        })
@EnableAutoConfiguration(
        exclude = {
            DataSourceAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class,
            DataJpaRepositoriesAutoConfiguration.class,
            EventPublicationAutoConfiguration.class
        },
        excludeName = JdbcEventPublicationAutoConfigurationExclusion.NAME)
class IamSecurityConfigurationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private AccessControlFacade accessControlFacade;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        reset(accessControlFacade);
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void shouldAllowPublicLoginEndpoint() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")).andExpect(status().isNotFound());
    }

    @Test
    void shouldAllowPublicHealthEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }

    @Test
    void shouldAllowGlobalTokenForSelectCompanyEndpoint() throws Exception {
        mockMvc.perform(post("/api/v1/auth/select-company").header("Authorization", "Bearer " + tokenWithUse("global")))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRequireAuthenticationForApiReads() throws Exception {
        mockMvc.perform(get("/api/v1/security-test"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.detail").value("Authentication is required to access this resource."))
                .andExpect(jsonPath("$.type").value("http://localhost/errors/unauthorized"))
                .andExpect(jsonPath("$.instance").value("/api/v1/security-test"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldAllowAuthenticatedApiReads() throws Exception {
        mockMvc.perform(get("/api/v1/security-test")
                        .with(jwt().jwt(builder -> builder.claim("token_use", "scoped")
                                        .claim("tenant_id", UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_MANAGER"))))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowAuthenticatedApiWritesThroughUrlSecurity() throws Exception {
        mockMvc.perform(post("/api/v1/security-test")
                        .with(jwt().jwt(builder -> builder.claim("token_use", "scoped")
                                        .claim("tenant_id", UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_MANAGER"))))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectScopedTokenWithoutTenantForBusinessApi() throws Exception {
        mockMvc.perform(get("/api/v1/security-test")
                        .with(jwt().jwt(builder -> builder.claim("token_use", "scoped"))
                                .authorities(new SimpleGrantedAuthority("ROLE_MANAGER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectGlobalTokenForBusinessApi() throws Exception {
        mockMvc.perform(get("/api/v1/security-test").header("Authorization", "Bearer " + tokenWithUse("global")))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectUnauthorizedMethodCalls() throws Exception {
        mockMvc.perform(post("/api/v1/method-security-test")
                        .with(jwt().jwt(builder -> builder.claim("token_use", "scoped")
                                        .claim("tenant_id", UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_MANAGER"))))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.detail").value("Access is denied for this resource."))
                .andExpect(jsonPath("$.type").value("http://localhost/errors/forbidden"))
                .andExpect(jsonPath("$.instance").value("/api/v1/method-security-test"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldAllowAuthorizedMethodCalls() throws Exception {
        mockMvc.perform(post("/api/v1/method-security-test")
                        .with(jwt().jwt(builder -> builder.claim("token_use", "scoped")
                                        .claim("tenant_id", UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(WRITE))))
                .andExpect(status().isOk());
    }

    @Test
    void shouldExposeTenantContextFromScopedToken() throws Exception {
        UUID tenantId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/security-test/tenant")
                        .header("Authorization", "Bearer " + tokenWithRole("ADMIN", tenantId)))
                .andExpect(status().isOk())
                .andExpect(content().string(tenantId.toString()));
    }

    @Test
    void shouldAllowAdminRoleClaimThroughUrlSecurity() throws Exception {
        mockMvc.perform(post("/api/v1/security-test").header("Authorization", "Bearer " + tokenWithRole("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowNonAdminRoleClaimThroughUrlSecurity() throws Exception {
        mockMvc.perform(post("/api/v1/security-test").header("Authorization", "Bearer " + tokenWithRole("MANAGER")))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAuthorizeManagerWithResolvedPermissionsAtMethodBoundary() throws Exception {
        String token = tokenWithRole("MANAGER");

        mockMvc.perform(post("/api/v1/method-security-test").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectOperatorWithoutWritePermissionAtMethodBoundary() throws Exception {
        String token = tokenWithRole("OPERATOR");

        mockMvc.perform(post("/api/v1/method-security-test").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAuthorizeAdminExplicitPermissionsAtMethodBoundary() throws Exception {
        String token = tokenWithRole("ADMIN");

        mockMvc.perform(post("/api/v1/method-security-test").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowUnknownRoleClaimThroughUrlSecurity() throws Exception {
        mockMvc.perform(get("/api/v1/security-test").header("Authorization", "Bearer " + tokenWithRole("UNKNOWN")))
                .andExpect(status().isOk());
    }

    @Test
    void shouldIgnoreAuthoritiesClaim() throws Exception {
        String token = tokenWithAuthorities("UNKNOWN", List.of("ROLE_UNKNOWN", WRITE), Set.of());

        mockMvc.perform(post("/api/v1/method-security-test").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldUseAuthoritativePermissionWhenTokenClaimDoesNotContainIt() throws Exception {
        String token = tokenWithAuthorities("ADMIN", List.of("ROLE_ADMIN"), Set.of(WRITE));

        mockMvc.perform(post("/api/v1/method-security-test").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDenyScopedTokenWithMissingAuthorizationClaimsInsteadOfFailing() throws Exception {
        String token = tokenWithAuthorizationClaimsMissing();

        mockMvc.perform(post("/api/v1/method-security-test").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    private String tokenWithAuthorizationClaimsMissing() {
        Instant issuedAt = Instant.now();
        UUID tenantId = UUID.randomUUID();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("erp-api")
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plusSeconds(3600))
                .subject(UUID.randomUUID().toString())
                .claim("token_use", "scoped")
                .claim("email", "user@example.com")
                .claim("tenant_id", tenantId.toString())
                .claim("company_id", tenantId.toString())
                .build();
        return jwtEncoder
                .encode(JwtEncoderParameters.from(
                        JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }

    private String tokenWithRole(String role) {
        return tokenWithRole(role, UUID.randomUUID());
    }

    private String tokenWithRole(String role, UUID tenantId) {
        Set<String> permissions =
                switch (role) {
                    case "ADMIN", "MANAGER" -> Set.of(WRITE);
                    default -> Set.of();
                };
        return tokenWithAuthorization(role, tenantId, List.of(), permissions);
    }

    private String tokenWithAuthorities(String role, List<String> claimedAuthorities, Set<String> permissions) {
        return tokenWithAuthorization(role, UUID.randomUUID(), claimedAuthorities, permissions);
    }

    private String tokenWithAuthorization(
            String role, UUID tenantId, List<String> claimedAuthorities, Set<String> permissions) {
        Instant issuedAt = Instant.now();
        UUID userId = UUID.randomUUID();
        UUID membershipId = UUID.randomUUID();
        long authorizationVersion = 3L;
        var authorization = new TenantAuthorizationSnapshot(
                membershipId,
                userId,
                tenantId,
                ACTIVE,
                authorizationVersion,
                List.of(new TenantAuthorizationSnapshot.RoleSummary(UUID.randomUUID(), role, role)),
                permissions);
        when(accessControlFacade.validateScopedToken(membershipId, userId, tenantId, authorizationVersion))
                .thenReturn(authorization);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("erp-api")
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plusSeconds(3600))
                .subject(userId.toString())
                .claim("token_use", "scoped")
                .claim("email", "user@example.com")
                .claim("tenant_id", tenantId.toString())
                .claim("company_id", tenantId.toString())
                .claim("membership_id", membershipId.toString())
                .claim("authz_version", authorizationVersion)
                .claim("roles", List.of(role))
                .claim("authorities", claimedAuthorities)
                .build();
        return jwtEncoder
                .encode(JwtEncoderParameters.from(
                        JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }

    private String tokenWithUse(String tokenUse) {
        Instant issuedAt = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("erp-api")
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plusSeconds(3600))
                .subject(UUID.randomUUID().toString())
                .claim("token_use", tokenUse)
                .claim("email", "user@example.com")
                .build();
        return jwtEncoder
                .encode(JwtEncoderParameters.from(
                        JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }

    @SpringBootApplication
    static class TestApplication {

        @Bean
        Clock clock() {
            return Clock.fixed(Instant.parse("2026-05-16T00:00:00Z"), ZoneOffset.UTC);
        }

        @Bean
        AccessControlFacade accessControlFacade() {
            return mock(AccessControlFacade.class);
        }
    }

    @RestController
    @RequestMapping("/api/v1/security-test")
    static class DummyController {

        @GetMapping
        String read() {
            return "read";
        }

        @GetMapping("/tenant")
        String tenant() {
            return TenantContext.requireTenantId().toString();
        }

        @PostMapping
        String write() {
            return "write";
        }
    }

    @RestController
    @RequestMapping("/api/v1/method-security-test")
    static class PermissionController {

        private final TestWriteUseCase useCase;

        PermissionController(TestWriteUseCase useCase) {
            this.useCase = useCase;
        }

        @PostMapping
        String write() {
            return useCase.execute();
        }
    }

    @Component
    static class TestWriteUseCase {

        @PreAuthorize("hasAuthority('" + WRITE + "')")
        String execute() {
            return "method write";
        }
    }
}

final class JdbcEventPublicationAutoConfigurationExclusion {

    static final String NAME =
            "org." + "springframework.modulith.events.jdbc." + "JdbcEventPublicationAutoConfiguration";

    private JdbcEventPublicationAutoConfigurationExclusion() {}
}
