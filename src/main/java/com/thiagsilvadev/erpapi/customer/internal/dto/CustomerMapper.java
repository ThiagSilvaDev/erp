package com.thiagsilvadev.erpapi.customer.internal.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.thiagsilvadev.erpapi.common.domain.model.Contact;
import com.thiagsilvadev.erpapi.common.domain.model.address.Address;
import com.thiagsilvadev.erpapi.common.domain.model.address.Country;
import com.thiagsilvadev.erpapi.customer.api.CustomerSnapshot;
import com.thiagsilvadev.erpapi.customer.api.SupplierSnapshot;
import com.thiagsilvadev.erpapi.customer.internal.model.ContactPerson;
import com.thiagsilvadev.erpapi.customer.internal.model.Customer;
import com.thiagsilvadev.erpapi.customer.internal.model.Supplier;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    CustomerResponse toResponse(Customer customer);

    SupplierResponse toResponse(Supplier supplier);

    ContactPersonResponse toResponse(ContactPerson contactPerson);

    CustomerSnapshot toSnapshot(Customer customer);

    SupplierSnapshot toSnapshot(Supplier supplier);

    default Contact toContact(ContactRequest request) {
        return request == null ? null : Contact.create(request.email(), request.phone());
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

    ContactResponse toResponse(Contact contact);

    @Mapping(target = "country", source = "country.name")
    AddressResponse toResponse(Address address);
}
