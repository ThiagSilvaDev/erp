package com.thiagsilvadev.erpapi.company.internal.seed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

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

@ExtendWith(MockitoExtension.class)
class CompanyDevSeederTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private CompanyContactRepository companyContactRepository;

    @Mock
    private BusinessNumberService businessNumberService;

    @Test
    void shouldSeedCompaniesAndContactsWhenMissing() {
        when(companyRepository.findByCnpj(any(Cnpj.class))).thenReturn(Optional.empty());
        when(businessNumberService.next(BusinessNumberType.COMPANY)).thenReturn(1L, 2L);
        when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> persisted(invocation.getArgument(0)));
        when(companyContactRepository.existsByCompanyId(any(UUID.class))).thenReturn(false);

        new CompanyDevSeeder(companyRepository, companyContactRepository, businessNumberService).run(null);

        verify(companyRepository, org.mockito.Mockito.times(2)).save(any(Company.class));

        ArgumentCaptor<CompanyContact> captor = ArgumentCaptor.forClass(CompanyContact.class);
        verify(companyContactRepository, org.mockito.Mockito.times(3)).save(captor.capture());

        assertThat(captor.getAllValues())
                .extracting(CompanyContact::getRole)
                .containsExactly("financial contact", "buyer", "manager");
        assertThat(captor.getAllValues())
                .filteredOn(CompanyContact::isPrimaryContact)
                .hasSize(2);
    }

    @Test
    void shouldSeedContactsForExistingCompaniesWithoutContacts() {
        Company acme = persisted(company("97.986.234/0001-91", CompanyType.FACTORY));
        Company partner = persisted(company("17.018.282/0001-57", CompanyType.CORRESPONDENT));
        when(companyRepository.findByCnpj(any(Cnpj.class))).thenReturn(Optional.of(acme), Optional.of(partner));
        when(companyContactRepository.existsByCompanyId(any(UUID.class))).thenReturn(false);

        new CompanyDevSeeder(companyRepository, companyContactRepository, businessNumberService).run(null);

        verify(companyRepository, never()).save(any(Company.class));
        verify(companyContactRepository, org.mockito.Mockito.times(3)).save(any(CompanyContact.class));
    }

    @Test
    void shouldSkipContactsWhenCompanyAlreadyHasContacts() {
        Company acme = persisted(company("97.986.234/0001-91", CompanyType.FACTORY));
        Company partner = persisted(company("17.018.282/0001-57", CompanyType.CORRESPONDENT));
        when(companyRepository.findByCnpj(any(Cnpj.class))).thenReturn(Optional.of(acme), Optional.of(partner));
        when(companyContactRepository.existsByCompanyId(any(UUID.class))).thenReturn(true);

        new CompanyDevSeeder(companyRepository, companyContactRepository, businessNumberService).run(null);

        verify(companyContactRepository, never()).save(any(CompanyContact.class));
    }

    private static Company persisted(Company company) {
        ReflectionTestUtils.setField(company, "id", UUID.randomUUID());
        return company;
    }

    private static Company company(String cnpj, CompanyType type) {
        return Company.create(
                1L,
                "Company",
                "Trade",
                Cnpj.create(cnpj),
                type,
                Address.builder()
                        .postalCode("01001000")
                        .street("Street")
                        .number("10")
                        .district("District")
                        .city("City")
                        .region("SP")
                        .country(Country.BR)
                        .build(),
                Contact.create("company@example.com", null));
    }
}
