package com.thiagsilvadev.erpapi.customer.internal.dto;

import org.springframework.stereotype.Component;

import com.thiagsilvadev.erpapi.common.domain.model.Contact;
import com.thiagsilvadev.erpapi.common.domain.model.address.Address;
import com.thiagsilvadev.erpapi.common.domain.model.address.Country;
import com.thiagsilvadev.erpapi.customer.api.CustomerSnapshot;
import com.thiagsilvadev.erpapi.customer.api.SupplierSnapshot;
import com.thiagsilvadev.erpapi.customer.internal.model.ContactPerson;
import com.thiagsilvadev.erpapi.customer.internal.model.Customer;
import com.thiagsilvadev.erpapi.customer.internal.model.Supplier;

@Component
public class CustomerMapper {

    public Customer toCustomer(CreateCustomerRequest request) {
        return Customer.create(
                request.name(),
                request.document(),
                request.companyId(),
                toContact(request.contact()),
                toAddress(request.address()));
    }

    public Supplier toSupplier(CreateSupplierRequest request) {
        return Supplier.create(
                request.name(),
                request.document(),
                request.companyId(),
                toContact(request.contact()),
                toAddress(request.address()));
    }

    public ContactPerson toContactPerson(CreateContactPersonRequest request) {
        return ContactPerson.create(request.name(), request.customerId(), toContact(request.contact()));
    }

    public CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getDocument(),
                customer.getCompanyId(),
                toResponse(customer.getContact()),
                toResponse(customer.getAddress()));
    }

    public SupplierResponse toResponse(Supplier supplier) {
        return new SupplierResponse(
                supplier.getId(),
                supplier.getName(),
                supplier.getDocument(),
                supplier.getCompanyId(),
                toResponse(supplier.getContact()),
                toResponse(supplier.getAddress()));
    }

    public ContactPersonResponse toResponse(ContactPerson contactPerson) {
        return new ContactPersonResponse(
                contactPerson.getId(),
                contactPerson.getName(),
                contactPerson.getCustomerId(),
                toResponse(contactPerson.getContact()));
    }

    public CustomerSnapshot toSnapshot(Customer customer) {
        return new CustomerSnapshot(
                customer.getId(), customer.getName(), customer.getDocument(), customer.getCompanyId());
    }

    public SupplierSnapshot toSnapshot(Supplier supplier) {
        return new SupplierSnapshot(
                supplier.getId(), supplier.getName(), supplier.getDocument(), supplier.getCompanyId());
    }

    private Contact toContact(ContactRequest request) {
        return request == null ? null : Contact.create(request.email(), request.phone());
    }

    private Address toAddress(AddressRequest request) {
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

    private ContactResponse toResponse(Contact contact) {
        return contact == null ? null : new ContactResponse(contact.getEmail(), contact.getPhone());
    }

    private AddressResponse toResponse(Address address) {
        return address == null
                ? null
                : new AddressResponse(
                        address.getPostalCode(),
                        address.getStreet(),
                        address.getNumber(),
                        address.getComplement(),
                        address.getDistrict(),
                        address.getCity(),
                        address.getRegion(),
                        address.getCountry().name());
    }
}
