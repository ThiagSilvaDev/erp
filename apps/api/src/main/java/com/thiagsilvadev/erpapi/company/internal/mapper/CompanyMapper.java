package com.thiagsilvadev.erpapi.company.internal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.thiagsilvadev.erpapi.common.domain.model.Cnpj;
import com.thiagsilvadev.erpapi.common.domain.model.Contact;
import com.thiagsilvadev.erpapi.common.domain.model.address.Address;
import com.thiagsilvadev.erpapi.common.domain.model.address.Country;
import com.thiagsilvadev.erpapi.common.domain.validation.Strings;
import com.thiagsilvadev.erpapi.company.api.CompanyContactSnapshot;
import com.thiagsilvadev.erpapi.company.api.CompanySnapshot;
import com.thiagsilvadev.erpapi.company.internal.dto.AddressRequest;
import com.thiagsilvadev.erpapi.company.internal.dto.AddressResponse;
import com.thiagsilvadev.erpapi.company.internal.dto.CompanyAddressResponse;
import com.thiagsilvadev.erpapi.company.internal.dto.CompanyContactResponse;
import com.thiagsilvadev.erpapi.company.internal.dto.CompanyResponse;
import com.thiagsilvadev.erpapi.company.internal.dto.ContactRequest;
import com.thiagsilvadev.erpapi.company.internal.dto.ContactResponse;
import com.thiagsilvadev.erpapi.company.internal.model.Company;
import com.thiagsilvadev.erpapi.company.internal.model.CompanyContact;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

    @Mapping(target = "primaryContact", ignore = true)
    CompanyResponse toResponse(Company company);

    @Mapping(target = "primaryContact", ignore = true)
    CompanySnapshot toSnapshot(Company company);

    CompanyContactResponse toResponse(CompanyContact companyContact);

    CompanyContactSnapshot toSnapshot(CompanyContact companyContact);

    default CompanyResponse toResponse(Company company, CompanyContact primaryContact) {
        if (company == null) {
            return null;
        }

        return new CompanyResponse(
                company.getId(),
                company.getCompanyCode(),
                company.getLegalName(),
                company.getTradeName(),
                company.getCnpj(),
                company.getStatus(),
                company.getType(),
                toContactResponse(company.getContact()),
                toAddressResponse(company.getAddress()),
                toResponse(primaryContact));
    }

    default CompanySnapshot toSnapshot(Company company, CompanyContact primaryContact) {
        if (company == null) {
            return null;
        }

        return new CompanySnapshot(
                company.getId(),
                company.getCompanyCode(),
                company.getLegalName(),
                company.getTradeName(),
                company.getCnpj(),
                company.getStatus(),
                company.getType(),
                company.getContact(),
                company.getAddress(),
                toSnapshot(primaryContact));
    }

    default CompanyAddressResponse toAddressResponse(Company company) {
        if (company == null) {
            return null;
        }

        return new CompanyAddressResponse(
                company.getId(),
                companyName(company),
                toAddressResponse(company.getAddress()),
                formatDeliveryRouteDestination(company.getAddress()));
    }

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

    default Cnpj toCnpj(String value) {
        return value == null ? null : Cnpj.create(value);
    }

    private String companyName(Company company) {
        String tradeName = Strings.trimToNull(company.getTradeName());
        return tradeName == null ? company.getLegalName() : tradeName;
    }

    private String formatDeliveryRouteDestination(Address address) {
        if (address == null) {
            return null;
        }

        StringBuilder destination =
                new StringBuilder().append(address.getStreet()).append(", ").append(address.getNumber());

        String complement = Strings.trimToNull(address.getComplement());
        if (complement != null) {
            destination.append(", ").append(complement);
        }

        return destination
                .append(" - ")
                .append(address.getDistrict())
                .append(", ")
                .append(address.getCity())
                .append(" - ")
                .append(address.getRegion())
                .append(", ")
                .append(address.getCountry().name())
                .append(", ")
                .append(address.getPostalCode())
                .toString();
    }
}
