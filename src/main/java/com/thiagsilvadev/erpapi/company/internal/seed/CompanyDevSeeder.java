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
import com.thiagsilvadev.erpapi.common.domain.numbering.BusinessNumberService;
import com.thiagsilvadev.erpapi.common.domain.numbering.BusinessNumberType;
import com.thiagsilvadev.erpapi.company.api.CompanyType;
import com.thiagsilvadev.erpapi.company.internal.model.Company;
import com.thiagsilvadev.erpapi.company.internal.model.CompanyContact;
import com.thiagsilvadev.erpapi.company.internal.repository.CompanyContactRepository;
import com.thiagsilvadev.erpapi.company.internal.repository.CompanyRepository;

@Component
@Profile("dev")
@Order(100)
class CompanyDevSeeder implements ApplicationRunner {

    private static final String ACME_CNPJ = "97.986.234/0001-91";
    private static final String PARTNER_CNPJ = "17.018.282/0001-57";

    private final CompanyRepository companyRepository;
    private final CompanyContactRepository companyContactRepository;
    private final BusinessNumberService businessNumberService;

    CompanyDevSeeder(
            CompanyRepository companyRepository,
            CompanyContactRepository companyContactRepository,
            BusinessNumberService businessNumberService) {
        this.companyRepository = companyRepository;
        this.companyContactRepository = companyContactRepository;
        this.businessNumberService = businessNumberService;
    }

    @Override
    @Transactional
    public void run(@NonNull ApplicationArguments args) {
        seedAcme();
        seedPartner();
    }

    private void seedAcme() {
        Cnpj cnpj = Cnpj.create(ACME_CNPJ);

        Company company = companyRepository.findByCnpj(cnpj).orElseGet(() -> createAcme(cnpj));

        seedAcmeContacts(company);
    }

    private Company createAcme(Cnpj cnpj) {
        Company company = Company.create(
                businessNumberService.next(BusinessNumberType.COMPANY),
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

        return companyRepository.save(company);
    }

    private void seedPartner() {
        Cnpj cnpj = Cnpj.create(PARTNER_CNPJ);

        Company company = companyRepository.findByCnpj(cnpj).orElseGet(() -> createPartner(cnpj));

        seedPartnerContacts(company);
    }

    private Company createPartner(Cnpj cnpj) {
        Company company = Company.create(
                businessNumberService.next(BusinessNumberType.COMPANY),
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

        return companyRepository.save(company);
    }

    private void seedAcmeContacts(Company company) {
        if (companyContactRepository.existsByCompanyId(company.getId())) {
            return;
        }

        companyContactRepository.save(CompanyContact.create(
                company.getId(),
                "Mariana Costa",
                "financial contact",
                Contact.create("mariana.costa@acme.example", "11922223333"),
                true));
        companyContactRepository.save(CompanyContact.create(
                company.getId(),
                "Rafael Almeida",
                "buyer",
                Contact.create("rafael.almeida@acme.example", "11933334444"),
                false));
    }

    private void seedPartnerContacts(Company company) {
        if (companyContactRepository.existsByCompanyId(company.getId())) {
            return;
        }

        companyContactRepository.save(CompanyContact.create(
                company.getId(),
                "Camila Souza",
                "manager",
                Contact.create("camila.souza@parceiro.example", "11944445555"),
                true));
    }
}
