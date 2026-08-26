package com.thiagsilvadev.erpapi.company.internal.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.domain.model.Cnpj;
import com.thiagsilvadev.erpapi.common.domain.model.Contact;
import com.thiagsilvadev.erpapi.common.domain.model.address.Address;
import com.thiagsilvadev.erpapi.common.domain.model.address.Country;
import com.thiagsilvadev.erpapi.company.api.CompanyStatus;
import com.thiagsilvadev.erpapi.company.api.CompanyType;
import com.thiagsilvadev.erpapi.company.internal.dto.AddressRequest;
import com.thiagsilvadev.erpapi.company.internal.dto.CompanyResponse;
import com.thiagsilvadev.erpapi.company.internal.dto.ContactRequest;
import com.thiagsilvadev.erpapi.company.internal.dto.UpdateCompanyRequest;
import com.thiagsilvadev.erpapi.company.internal.mapper.CompanyMapper;
import com.thiagsilvadev.erpapi.company.internal.model.Company;
import com.thiagsilvadev.erpapi.company.internal.repository.CompanyContactRepository;
import com.thiagsilvadev.erpapi.company.internal.repository.CompanyRepository;

@ExtendWith(MockitoExtension.class)
class UpdateCompanyUseCaseTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private CompanyContactRepository companyContactRepository;

    @Mock
    private CompanyMapper companyMapper;

    @Test
    void shouldUpdateCompany() {
        UUID companyId = UUID.randomUUID();
        Company company = company();
        UpdateCompanyRequest request = updateRequest();
        Address newAddress = address();
        Contact newContact = Contact.create("updated@example.com", "11999999999");
        CompanyResponse expectedResponse = new CompanyResponse(
                companyId,
                1L,
                "Updated Legal",
                "Updated Trade",
                company.getCnpj(),
                CompanyStatus.ACTIVE,
                CompanyType.FACTORY,
                null,
                null,
                null);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(companyMapper.toAddress(request.address())).thenReturn(newAddress);
        when(companyMapper.toContact(request.contact())).thenReturn(newContact);
        when(companyContactRepository.findByCompanyIdAndPrimaryContactTrue(companyId))
                .thenReturn(Optional.empty());
        when(companyMapper.toResponse(company, null)).thenReturn(expectedResponse);

        CompanyResponse response = new UpdateCompanyUseCase(companyRepository, companyContactRepository, companyMapper)
                .execute(companyId, request);

        assertThat(response).isEqualTo(expectedResponse);
        assertThat(company.getLegalName()).isEqualTo("Updated Legal");
        assertThat(company.getTradeName()).isEqualTo("Updated Trade");
        assertThat(company.getType()).isEqualTo(CompanyType.FACTORY);
        assertThat(company.getAddress()).isEqualTo(newAddress);
        assertThat(company.getContact()).isEqualTo(newContact);
    }

    @Test
    void shouldThrowWhenCompanyDoesNotExist() {
        UUID companyId = UUID.randomUUID();
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());
        UpdateCompanyUseCase useCase =
                new UpdateCompanyUseCase(companyRepository, companyContactRepository, companyMapper);

        var exception = catchThrowableOfType(
                ResourceNotFoundException.class, () -> useCase.execute(companyId, updateRequest()));

        assertThat(exception.getProperties()).containsEntry("resourceName", "Company");
    }

    private static Company company() {
        return Company.create(
                1L,
                "Legal Name",
                "Trade Name",
                Cnpj.create("11222333000181"),
                CompanyType.CORRESPONDENT,
                address(),
                Contact.create("company@example.com", null));
    }

    private static UpdateCompanyRequest updateRequest() {
        return new UpdateCompanyRequest(
                "Updated Legal",
                "Updated Trade",
                CompanyType.FACTORY,
                new ContactRequest("updated@example.com", "11999999999"),
                new AddressRequest(
                        "01310100", "Avenida Paulista", "1000", null, "Bela Vista", "Sao Paulo", "SP", "BR"));
    }

    private static Address address() {
        return Address.builder()
                .postalCode("01310100")
                .street("Avenida Paulista")
                .number("1000")
                .district("Bela Vista")
                .city("Sao Paulo")
                .region("SP")
                .country(Country.BR)
                .build();
    }
}
