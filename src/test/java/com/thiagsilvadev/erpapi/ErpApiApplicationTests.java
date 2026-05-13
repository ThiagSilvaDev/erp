package com.thiagsilvadev.erpapi;

import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

import com.thiagsilvadev.erpapi.catalog.internal.repository.CategoryRepository;
import com.thiagsilvadev.erpapi.catalog.internal.repository.ProductRepository;
import com.thiagsilvadev.erpapi.company.internal.repository.CompanyRepository;
import com.thiagsilvadev.erpapi.iam.internal.repository.IamCredentialRepository;
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
    }
}
