package com.thiagsilvadev.erpapi.customer.internal.seed;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.model.Contact;
import com.thiagsilvadev.erpapi.common.model.address.Address;
import com.thiagsilvadev.erpapi.common.model.address.Country;
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
    static final String SUPPLIER_DOCUMENT = "22333444000150";
    private static final String ACME_CNPJ = "11.222.333/0001-81";

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
    public void run(ApplicationArguments args) {
        var company = companyFacade
                .findByCnpj(ACME_CNPJ)
                .orElseThrow(() -> new IllegalStateException("Seed company must exist before seeding customers"));

        Customer customer = customerRepository
                .findByDocument(CUSTOMER_DOCUMENT)
                .orElseGet(() -> customerRepository.save(Customer.create(
                        "Acme Retail Customer",
                        CUSTOMER_DOCUMENT,
                        company.id(),
                        Contact.create("buyer@customer.example", "11911112222"),
                        address("2000"))));

        if (contactPersonRepository.count() == 0) {
            contactPersonRepository.save(ContactPerson.create(
                    "Maria Buyer", customer.getId(), Contact.create("maria@customer.example", "11933334444")));
        }

        if (!supplierRepository.existsByDocument(SUPPLIER_DOCUMENT)) {
            supplierRepository.save(Supplier.create(
                    "Industrial Supplier",
                    SUPPLIER_DOCUMENT,
                    company.id(),
                    Contact.create("sales@supplier.example", "11955556666"),
                    address("3000")));
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
