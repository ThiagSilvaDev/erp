package com.thiagsilvadev.erpapi.company.internal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.thiagsilvadev.erpapi.common.domain.model.Cnpj;
import com.thiagsilvadev.erpapi.common.domain.model.Contact;
import com.thiagsilvadev.erpapi.common.domain.model.address.Address;
import com.thiagsilvadev.erpapi.common.domain.model.address.Country;
import com.thiagsilvadev.erpapi.company.api.CompanySnapshot;
import com.thiagsilvadev.erpapi.company.internal.dto.AddressRequest;
import com.thiagsilvadev.erpapi.company.internal.dto.AddressResponse;
import com.thiagsilvadev.erpapi.company.internal.dto.CompanyResponse;
import com.thiagsilvadev.erpapi.company.internal.dto.ContactRequest;
import com.thiagsilvadev.erpapi.company.internal.dto.ContactResponse;
import com.thiagsilvadev.erpapi.company.internal.dto.CreateCompanyRequest;
import com.thiagsilvadev.erpapi.company.internal.model.Company;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

    CompanyResponse toResponse(Company company);

    CompanySnapshot toSnapshot(Company company);

    default Address toAddress(AddressRequest request) {
        if (request == null) {
            return null;
        }

        return Address.builder()
                .postalCode(request.postalCode())
                .street(request.street())
                .number(request.number())
                .complement(request.complement())
                .district(request.district())
                .city(request.city())
                .region(request.region())
                .country(Country.valueOf(request.country()))
                .build();
    }

    @Mapping(target = "country", source = "country.name")
    AddressResponse toAddressResponse(Address address);

    default Contact toContact(ContactRequest request) {
        if (request == null) {
            return null;
        }

        return Contact.create(request.email(), request.phone());
    }

    ContactResponse toContactResponse(Contact contact);

    default Company toEntity(CreateCompanyRequest request, Cnpj cnpj) {
        if (request == null) {
            return null;
        }
        return Company.create(
                trimToNull(request.legalName()),
                trimToNull(request.tradeName()),
                cnpj,
                request.type(),
                toAddress(request.address()),
                toContact(request.contact()));
    }

    @Named("trimToNull")
    default String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }
}
