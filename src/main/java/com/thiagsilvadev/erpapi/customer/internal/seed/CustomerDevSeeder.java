package com.thiagsilvadev.erpapi.customer.internal.seed;

import java.util.UUID;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.model.Contact;
import com.thiagsilvadev.erpapi.common.domain.model.address.Address;
import com.thiagsilvadev.erpapi.common.domain.model.address.Country;
import com.thiagsilvadev.erpapi.company.api.CompanyFacade;
import com.thiagsilvadev.erpapi.customer.internal.model.ContactPerson;
import com.thiagsilvadev.erpapi.customer.internal.model.Customer;
import com.thiagsilvadev.erpapi.customer.internal.model.Supplier;
import com.thiagsilvadev.erpapi.customer.internal.repository.ContactPersonRepository;
import com.thiagsilvadev.erpapi.customer.internal.repository.CustomerRepository;
import com.thiagsilvadev.erpapi.customer.internal.repository.SupplierRepository;

@Component
@Profile("dev")
@Order(400)
class CustomerDevSeeder implements ApplicationRunner {

    static final String CUSTOMER_DOCUMENT = "12345678901";
    static final String PARTNER_CUSTOMER_DOCUMENT = "98765432100";
    static final String SUPPLIER_DOCUMENT = "22333444000150";
    private static final String ACME_CNPJ = "97.986.234/0001-91";
    private static final String PARTNER_CNPJ = "17.018.282/0001-57";

    private final CustomerRepository customerRepository;
    private final SupplierRepository supplierRepository;
    private final ContactPersonRepository contactPersonRepository;
    private final CompanyFacade companyFacade;

    CustomerDevSeeder(
            CustomerRepository customerRepository,
            SupplierRepository supplierRepository,
            ContactPersonRepository contactPersonRepository,
            CompanyFacade companyFacade) {
        this.customerRepository = customerRepository;
        this.supplierRepository = supplierRepository;
        this.contactPersonRepository = contactPersonRepository;
        this.companyFacade = companyFacade;
    }

    @Override
    @Transactional
    public void run(@NonNull ApplicationArguments args) {
        var acme = companyFacade.resolveCompany(ACME_CNPJ);
        var partner = companyFacade.resolveCompany(PARTNER_CNPJ);

        seedAcme(acme.id());
        seedPartner(partner.id());
    }

    private void seedAcme(UUID companyId) {
        Customer customer = seedCustomer(
                companyId,
                "Acme Retail Customer",
                CUSTOMER_DOCUMENT,
                Contact.create("buyer@customer.example", "11911112222"),
                address("2000"));

        seedContactPerson(customer.getId(), "Maria Buyer", Contact.create("maria@customer.example", "11933334444"));

        if (!supplierRepository.existsByDocument(SUPPLIER_DOCUMENT)) {
            supplierRepository.save(Supplier.create(
                    "Industrial Supplier",
                    SUPPLIER_DOCUMENT,
                    companyId,
                    Contact.create("sales@supplier.example", "11955556666"),
                    address("3000")));
        }
    }

    private void seedPartner(UUID companyId) {
        Customer customer = seedCustomer(
                companyId,
                "Parceiro Retail Customer",
                PARTNER_CUSTOMER_DOCUMENT,
                Contact.create("buyer@parceiro-customer.example", "11966667777"),
                address("4000"));

        seedContactPerson(
                customer.getId(), "Ana Buyer", Contact.create("ana@parceiro-customer.example", "11977778888"));
    }

    private Customer seedCustomer(UUID companyId, String name, String document, Contact contact, Address address) {
        String normalizedDocument = Customer.normalizeDocument(document);
        return customerRepository
                .findByOwnerCompanyIdAndDocument(companyId, normalizedDocument)
                .orElseGet(() -> customerRepository.save(Customer.create(name, document, companyId, contact, address)));
    }

    private void seedContactPerson(UUID customerId, String name, Contact contact) {
        if (!contactPersonRepository.existsByCustomerId(customerId)) {
            contactPersonRepository.save(ContactPerson.create(name, customerId, contact));
        }
    }

    private Address address(String number) {
        return Address.builder()
                .postalCode("01310-100")
                .street("Avenida Paulista")
                .number(number)
                .district("Bela Vista")
                .city("Sao Paulo")
                .region("SP")
                .country(Country.BR)
                .build();
    }
}
