package com.thiagsilvadev.erpapi.company.internal.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.thiagsilvadev.erpapi.common.domain.model.Cnpj;
import com.thiagsilvadev.erpapi.common.domain.model.Contact;
import com.thiagsilvadev.erpapi.common.domain.model.address.Address;
import com.thiagsilvadev.erpapi.common.domain.model.address.Country;
import com.thiagsilvadev.erpapi.company.api.CompanyType;
import com.thiagsilvadev.erpapi.company.internal.model.Company;

class CompanyMapperTest {

    private final CompanyMapper mapper = new CompanyMapperImpl();

    @Test
    void shouldFormatCompanyAddressForDeliveryRoutePrefill() {
        Company company = Company.create(
                1L,
                "Legal Name",
                "Trade Name",
                Cnpj.create("11222333000181"),
                CompanyType.CORRESPONDENT,
                address(),
                Contact.create("company@example.com", null));

        var response = mapper.toAddressResponse(company);

        assertThat(response.companyName()).isEqualTo("Trade Name");
        assertThat(response.address().street()).isEqualTo("Avenida Paulista");
        assertThat(response.deliveryRouteDestinationDescription())
                .isEqualTo("Avenida Paulista, 1000, 10th floor - Bela Vista, Sao Paulo - SP, BR, 01310100");
    }

    private static Address address() {
        return Address.builder()
                .postalCode("01310100")
                .street("Avenida Paulista")
                .number("1000")
                .complement("10th floor")
                .district("Bela Vista")
                .city("Sao Paulo")
                .region("SP")
                .country(Country.BR)
                .build();
    }
}
