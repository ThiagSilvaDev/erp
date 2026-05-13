package com.thiagsilvadev.erpapi.company.internal.seed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.thiagsilvadev.erpapi.common.model.Cnpj;
import com.thiagsilvadev.erpapi.company.internal.model.Company;
import com.thiagsilvadev.erpapi.company.internal.repository.CompanyRepository;

@ExtendWith(MockitoExtension.class)
class CompanyDevSeederTest {

    @Mock
    private CompanyRepository companyRepository;

    @Test
    void shouldSeedAcmeCompanyWhenMissing() {
        Cnpj cnpj = Cnpj.create(CompanyDevSeeder.ACME_CNPJ);
        when(companyRepository.existsByCnpj(cnpj)).thenReturn(false);

        new CompanyDevSeeder(companyRepository).run(null);

        ArgumentCaptor<Company> captor = ArgumentCaptor.forClass(Company.class);
        verify(companyRepository).save(captor.capture());

        Company company = captor.getValue();
        assertThat(company.getLegalName()).isEqualTo("Acme Industria Ltda");
        assertThat(company.getTradeName()).isEqualTo("Acme");
        assertThat(company.getCnpj()).isEqualTo(cnpj);
        assertThat(company.getContact().getEmail()).isEqualTo("financeiro@acme.example");
        assertThat(company.getAddress().getCity()).isEqualTo("Sao Paulo");
    }

    @Test
    void shouldSkipAcmeCompanyWhenAlreadySeeded() {
        Cnpj cnpj = Cnpj.create(CompanyDevSeeder.ACME_CNPJ);
        when(companyRepository.existsByCnpj(cnpj)).thenReturn(true);

        new CompanyDevSeeder(companyRepository).run(null);

        verify(companyRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
