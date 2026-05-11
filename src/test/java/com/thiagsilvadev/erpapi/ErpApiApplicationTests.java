package com.thiagsilvadev.erpapi;

import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

import com.thiagsilvadev.erpapi.company.internal.repository.CompanyRepository;

@SpringBootTest(
        properties = {
            "spring.autoconfigure.exclude="
                    + "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,"
                    + "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration,"
                    + "org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration,"
                    + "org.springframework.modulith.events.config.EventPublicationAutoConfiguration,"
                    + "org.springframework.modulith.events.jpa.JpaEventPublicationAutoConfiguration"
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
    }
}
