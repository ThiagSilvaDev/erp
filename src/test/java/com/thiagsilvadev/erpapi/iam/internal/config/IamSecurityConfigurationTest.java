package com.thiagsilvadev.erpapi.iam.internal.config;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(
        classes = {
            IamSecurityConfigurationTest.TestApplication.class,
            IamSecurityConfigurationTest.DummyController.class,
            IamSecurityConfiguration.class
        },
        properties = {
            "erp.iam.jwt.secret=test-secret-for-iam-jwt-signing-must-be-32-bytes",
            "spring.autoconfigure.exclude="
                    + "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,"
                    + "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration,"
                    + "org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration,"
                    + "org.springframework.modulith.events.config.EventPublicationAutoConfiguration,"
                    + "org.springframework.modulith.events.jpa.JpaEventPublicationAutoConfiguration"
        })
class IamSecurityConfigurationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void shouldAllowPublicLoginEndpoint() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")).andExpect(status().isNotFound());
    }

    @Test
    void shouldRequireAuthenticationForApiReads() throws Exception {
        mockMvc.perform(get("/api/v1/security-test")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowAuthenticatedApiReads() throws Exception {
        mockMvc.perform(get("/api/v1/security-test")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_MANAGER"))))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectNonAdminApiWrites() throws Exception {
        mockMvc.perform(post("/api/v1/security-test")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_MANAGER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAdminApiWrites() throws Exception {
        mockMvc.perform(post("/api/v1/security-test").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());
    }

    @SpringBootApplication
    static class TestApplication {}

    @RestController
    @RequestMapping("/api/v1/security-test")
    static class DummyController {

        @GetMapping
        String read() {
            return "read";
        }

        @PostMapping
        String write() {
            return "write";
        }
    }
}
