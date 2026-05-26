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
import com.thiagsilvadev.erpapi.company.api.CompanyType;
import com.thiagsilvadev.erpapi.company.internal.dto.AddressResponse;
import com.thiagsilvadev.erpapi.company.internal.dto.CompanyAddressResponse;
import com.thiagsilvadev.erpapi.company.internal.mapper.CompanyMapper;
import com.thiagsilvadev.erpapi.company.internal.model.Company;
import com.thiagsilvadev.erpapi.company.internal.repository.CompanyRepository;

@ExtendWith(MockitoExtension.class)
class GetCompanyAddressUseCaseTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private CompanyMapper companyMapper;

    @Test
    void shouldReturnCompanyAddressPrefill() {
        UUID companyId = UUID.randomUUID();
        Company company = company();
        CompanyAddressResponse expectedResponse = new CompanyAddressResponse(
                companyId,
                "Trade Name",
                new AddressResponse(
                        "01310100", "Avenida Paulista", "1000", null, "Bela Vista", "Sao Paulo", "SP", "BR"),
                "Avenida Paulista, 1000 - Bela Vista, Sao Paulo - SP, BR, 01310100");
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(companyMapper.toAddressResponse(company)).thenReturn(expectedResponse);

        CompanyAddressResponse response =
                new GetCompanyAddressUseCase(companyRepository, companyMapper).execute(companyId);

        assertThat(response).isEqualTo(expectedResponse);
    }

    @Test
    void shouldThrowWhenCompanyDoesNotExist() {
        UUID companyId = UUID.randomUUID();
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());
        GetCompanyAddressUseCase useCase = new GetCompanyAddressUseCase(companyRepository, companyMapper);

        var exception = catchThrowableOfType(ResourceNotFoundException.class, () -> useCase.execute(companyId));

        assertThat(exception.getProperties()).containsEntry("resourceName", "Company");
    }

    private static Company company() {
        return Company.create(
                1L,
                "Legal Name",
                "Trade Name",
                Cnpj.create("11222333000181"),
                CompanyType.CORRESPONDENT,
                Address.builder()
                        .postalCode("01310100")
                        .street("Avenida Paulista")
                        .number("1000")
                        .district("Bela Vista")
                        .city("Sao Paulo")
                        .region("SP")
                        .country(Country.BR)
                        .build(),
                Contact.create("company@example.com", null));
    }
}
