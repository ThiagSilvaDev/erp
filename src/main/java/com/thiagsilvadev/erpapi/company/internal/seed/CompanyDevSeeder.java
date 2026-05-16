package com.thiagsilvadev.erpapi.company.internal.seed;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.model.Cnpj;
import com.thiagsilvadev.erpapi.common.domain.model.Contact;
import com.thiagsilvadev.erpapi.common.domain.model.address.Address;
import com.thiagsilvadev.erpapi.common.domain.model.address.Country;
import com.thiagsilvadev.erpapi.company.api.CompanyType;
import com.thiagsilvadev.erpapi.company.internal.model.Company;
import com.thiagsilvadev.erpapi.company.internal.repository.CompanyRepository;

@Component
@Profile("dev")
@Order(100)
class CompanyDevSeeder implements ApplicationRunner {

    private static final String ACME_CNPJ = "97.986.234/0001-91";
    private static final String PARTNER_CNPJ = "17.018.282/0001-57";

    private final CompanyRepository companyRepository;

    CompanyDevSeeder(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    @Transactional
    public void run(@NonNull ApplicationArguments args) {
        seedAcme();
        seedPartner();
    }

    private void seedAcme() {
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

    private void seedPartner() {
        Cnpj cnpj = Cnpj.create(PARTNER_CNPJ);

        if (companyRepository.existsByCnpj(cnpj)) {
            return;
        }

        Company company = Company.create(
                "Parceiro Correspondente S.A.",
                "Parceiro",
                cnpj,
                CompanyType.CORRESPONDENT,
                Address.builder()
                        .postalCode("04538-133")
                        .street("Avenida Brigadeiro Faria Lima")
                        .number("3000")
                        .complement("Andar 5")
                        .district("Itaim Bibi")
                        .city("Sao Paulo")
                        .region("SP")
                        .country(Country.BR)
                        .build(),
                Contact.create("contato@parceiro.example", "11987654321"));

        companyRepository.save(company);
    }
}
