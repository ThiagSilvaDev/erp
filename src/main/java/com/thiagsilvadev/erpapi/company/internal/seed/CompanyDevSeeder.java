package com.thiagsilvadev.erpapi.company.internal.seed;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.model.Cnpj;
import com.thiagsilvadev.erpapi.common.model.Contact;
import com.thiagsilvadev.erpapi.common.model.address.Address;
import com.thiagsilvadev.erpapi.common.model.address.Country;
import com.thiagsilvadev.erpapi.company.api.CompanyType;
import com.thiagsilvadev.erpapi.company.internal.model.Company;
import com.thiagsilvadev.erpapi.company.internal.repository.CompanyRepository;

@Component
@Profile("dev")
@Order(100)
class CompanyDevSeeder implements ApplicationRunner {

    static final String ACME_CNPJ = "11.222.333/0001-81";

    private final CompanyRepository companyRepository;

    CompanyDevSeeder(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Cnpj cnpj = Cnpj.create(ACME_CNPJ);
        if (companyRepository.existsByCnpj(cnpj)) {
            return;
        }

        Company company = Company.create(
                "Acme Industria Ltda",
                "Acme",
                cnpj,
                CompanyType.FACTORY,
                Address.builder()
                        .postalCode("01310-100")
                        .street("Avenida Paulista")
                        .number("1000")
                        .complement("Conjunto 101")
                        .district("Bela Vista")
                        .city("Sao Paulo")
                        .region("SP")
                        .country(Country.BR)
                        .build(),
                Contact.create("financeiro@acme.example", "11912345678"));

        companyRepository.save(company);
    }
}
