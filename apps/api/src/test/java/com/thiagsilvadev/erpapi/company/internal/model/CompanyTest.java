package com.thiagsilvadev.erpapi.company.internal.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.thiagsilvadev.erpapi.common.domain.exception.InvalidAttributeException;
import com.thiagsilvadev.erpapi.common.domain.model.Cnpj;
import com.thiagsilvadev.erpapi.common.domain.model.Contact;
import com.thiagsilvadev.erpapi.common.domain.model.address.Address;
import com.thiagsilvadev.erpapi.common.domain.model.address.Country;
import com.thiagsilvadev.erpapi.company.api.CompanyStatus;
import com.thiagsilvadev.erpapi.company.api.CompanyType;

class CompanyTest {

    @Test
    void shouldCreateValidCompany() {
        Company company = Company.create(
                1L,
                " Acme Industria Ltda ",
                " Acme ",
                Cnpj.create("11222333000181"),
                CompanyType.FACTORY,
                address(),
                Contact.create("company@example.com", null));

        assertThat(company.getCompanyCode()).isEqualTo(1L);
        assertThat(company.getLegalName()).isEqualTo("Acme Industria Ltda");
        assertThat(company.getTradeName()).isEqualTo("Acme");
        assertThat(company.getType()).isEqualTo(CompanyType.FACTORY);
        assertThat(company.getStatus()).isEqualTo(CompanyStatus.ACTIVE);
    }

    @Test
    void shouldFailWhenCompanyCodeIsNull() {
        assertThatThrownBy(() -> Company.create(
                        null, "Acme", "Acme", Cnpj.create("11222333000181"), CompanyType.FACTORY, address(), contact()))
                .isInstanceOf(InvalidAttributeException.class)
                .hasMessage("Company code cannot be null");
    }

    @Test
    void shouldFailWhenCnpjIsNull() {
        assertThatThrownBy(() -> Company.create(1L, "Acme", "Acme", null, CompanyType.FACTORY, address(), contact()))
                .isInstanceOf(InvalidAttributeException.class)
                .hasMessage("CNPJ cannot be null");
    }

    @Test
    void shouldFailWhenTypeIsNull() {
        assertThatThrownBy(() ->
                        Company.create(1L, "Acme", "Acme", Cnpj.create("11222333000181"), null, address(), contact()))
                .isInstanceOf(InvalidAttributeException.class)
                .hasMessage("Company type cannot be null");
    }

    @Test
    void shouldRejectNullTypeOnChange() {
        Company company = company();

        assertThatThrownBy(() -> company.changeType(null))
                .isInstanceOf(InvalidAttributeException.class)
                .hasMessage("Company type cannot be null");
    }

    @Test
    void shouldRejectNullAddressOnChange() {
        Company company = company();

        assertThatThrownBy(() -> company.changeAddress(null))
                .isInstanceOf(InvalidAttributeException.class)
                .hasMessage("Address cannot be null");
    }

    @Test
    void shouldRejectNullContactOnChange() {
        Company company = company();

        assertThatThrownBy(() -> company.changeContact(null))
                .isInstanceOf(InvalidAttributeException.class)
                .hasMessage("Contact cannot be null");
    }

    private static Company company() {
        return Company.create(
                1L, "Acme", "Acme", Cnpj.create("11222333000181"), CompanyType.FACTORY, address(), contact());
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

    private static Contact contact() {
        return Contact.create("company@example.com", null);
    }
}
