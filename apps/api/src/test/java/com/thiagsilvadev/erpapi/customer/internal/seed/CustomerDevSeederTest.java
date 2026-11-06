package com.thiagsilvadev.erpapi.customer.internal.seed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.thiagsilvadev.erpapi.common.domain.model.Contact;
import com.thiagsilvadev.erpapi.common.domain.model.address.Address;
import com.thiagsilvadev.erpapi.common.domain.model.address.Country;
import com.thiagsilvadev.erpapi.company.api.CompanyFacade;
import com.thiagsilvadev.erpapi.company.api.CompanySnapshot;
import com.thiagsilvadev.erpapi.customer.internal.model.ContactPerson;
import com.thiagsilvadev.erpapi.customer.internal.model.Customer;
import com.thiagsilvadev.erpapi.customer.internal.model.Supplier;
import com.thiagsilvadev.erpapi.customer.internal.repository.ContactPersonRepository;
import com.thiagsilvadev.erpapi.customer.internal.repository.CustomerRepository;
import com.thiagsilvadev.erpapi.customer.internal.repository.SupplierRepository;

@ExtendWith(MockitoExtension.class)
class CustomerDevSeederTest {

    private static final UUID ACME_ID = UUID.randomUUID();
    private static final UUID PARTNER_ID = UUID.randomUUID();

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private ContactPersonRepository contactPersonRepository;

    @Mock
    private CompanyFacade companyFacade;

    @Test
    void shouldSeedCustomersContactPersonsAndSupplierWhenMissing() {
        when(companyFacade.resolveCompany("97.986.234/0001-91")).thenReturn(company(ACME_ID));
        when(companyFacade.resolveCompany("17.018.282/0001-57")).thenReturn(company(PARTNER_ID));
        when(customerRepository.findByOwnerCompanyIdAndDocument(eq(ACME_ID), eq(CustomerDevSeeder.CUSTOMER_DOCUMENT)))
                .thenReturn(Optional.empty());
        when(customerRepository.findByOwnerCompanyIdAndDocument(
                        eq(PARTNER_ID), eq(CustomerDevSeeder.PARTNER_CUSTOMER_DOCUMENT)))
                .thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class)))
                .thenAnswer(invocation -> persisted(invocation.getArgument(0)));
        when(contactPersonRepository.existsByCustomerId(any(UUID.class))).thenReturn(false);
        when(supplierRepository.existsByDocument(CustomerDevSeeder.SUPPLIER_DOCUMENT))
                .thenReturn(false);

        new CustomerDevSeeder(customerRepository, supplierRepository, contactPersonRepository, companyFacade).run(null);

        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository, times(2)).save(customerCaptor.capture());
        assertThat(customerCaptor.getAllValues())
                .extracting(Customer::getName)
                .containsExactly("Acme Retail Customer", "Parceiro Retail Customer");
        assertThat(customerCaptor.getAllValues())
                .extracting(Customer::getOwnerCompanyId)
                .containsExactly(ACME_ID, PARTNER_ID);
        assertThat(customerCaptor.getAllValues())
                .extracting(Customer::getDocument)
                .containsExactly(CustomerDevSeeder.CUSTOMER_DOCUMENT, CustomerDevSeeder.PARTNER_CUSTOMER_DOCUMENT);

        ArgumentCaptor<ContactPerson> contactCaptor = ArgumentCaptor.forClass(ContactPerson.class);
        verify(contactPersonRepository, times(2)).save(contactCaptor.capture());
        assertThat(contactCaptor.getAllValues())
                .extracting(ContactPerson::getName)
                .containsExactly("Maria Buyer", "Ana Buyer");

        ArgumentCaptor<Supplier> supplierCaptor = ArgumentCaptor.forClass(Supplier.class);
        verify(supplierRepository).save(supplierCaptor.capture());
        assertThat(supplierCaptor.getValue().getDocument()).isEqualTo(CustomerDevSeeder.SUPPLIER_DOCUMENT);
        assertThat(supplierCaptor.getValue().getCompanyId()).isEqualTo(ACME_ID);
    }

    @Test
    void shouldSkipWhenAlreadySeeded() {
        Customer acmeCustomer = persisted(Customer.create(
                "Acme Retail Customer",
                CustomerDevSeeder.CUSTOMER_DOCUMENT,
                ACME_ID,
                Contact.create("buyer@customer.example", "11911112222"),
                address("2000")));
        Customer partnerCustomer = persisted(Customer.create(
                "Parceiro Retail Customer",
                CustomerDevSeeder.PARTNER_CUSTOMER_DOCUMENT,
                PARTNER_ID,
                Contact.create("buyer@parceiro-customer.example", "11966667777"),
                address("4000")));

        when(companyFacade.resolveCompany("97.986.234/0001-91")).thenReturn(company(ACME_ID));
        when(companyFacade.resolveCompany("17.018.282/0001-57")).thenReturn(company(PARTNER_ID));
        when(customerRepository.findByOwnerCompanyIdAndDocument(eq(ACME_ID), eq(CustomerDevSeeder.CUSTOMER_DOCUMENT)))
                .thenReturn(Optional.of(acmeCustomer));
        when(customerRepository.findByOwnerCompanyIdAndDocument(
                        eq(PARTNER_ID), eq(CustomerDevSeeder.PARTNER_CUSTOMER_DOCUMENT)))
                .thenReturn(Optional.of(partnerCustomer));
        when(contactPersonRepository.existsByCustomerId(acmeCustomer.getId())).thenReturn(true);
        when(contactPersonRepository.existsByCustomerId(partnerCustomer.getId()))
                .thenReturn(true);
        when(supplierRepository.existsByDocument(CustomerDevSeeder.SUPPLIER_DOCUMENT))
                .thenReturn(true);

        new CustomerDevSeeder(customerRepository, supplierRepository, contactPersonRepository, companyFacade).run(null);

        verify(customerRepository, never()).save(any());
        verify(contactPersonRepository, never()).save(any());
        verify(supplierRepository, never()).save(any());
    }

    @Test
    void shouldSeedAcmeSupplierWhenMissingEvenIfCustomersExist() {
        Customer acmeCustomer = persisted(Customer.create(
                "Acme Retail Customer",
                CustomerDevSeeder.CUSTOMER_DOCUMENT,
                ACME_ID,
                Contact.create("buyer@customer.example", "11911112222"),
                address("2000")));
        Customer partnerCustomer = persisted(Customer.create(
                "Parceiro Retail Customer",
                CustomerDevSeeder.PARTNER_CUSTOMER_DOCUMENT,
                PARTNER_ID,
                Contact.create("buyer@parceiro-customer.example", "11966667777"),
                address("4000")));

        when(companyFacade.resolveCompany("97.986.234/0001-91")).thenReturn(company(ACME_ID));
        when(companyFacade.resolveCompany("17.018.282/0001-57")).thenReturn(company(PARTNER_ID));
        when(customerRepository.findByOwnerCompanyIdAndDocument(eq(ACME_ID), eq(CustomerDevSeeder.CUSTOMER_DOCUMENT)))
                .thenReturn(Optional.of(acmeCustomer));
        when(customerRepository.findByOwnerCompanyIdAndDocument(
                        eq(PARTNER_ID), eq(CustomerDevSeeder.PARTNER_CUSTOMER_DOCUMENT)))
                .thenReturn(Optional.of(partnerCustomer));
        when(contactPersonRepository.existsByCustomerId(acmeCustomer.getId())).thenReturn(true);
        when(contactPersonRepository.existsByCustomerId(partnerCustomer.getId()))
                .thenReturn(true);
        when(supplierRepository.existsByDocument(CustomerDevSeeder.SUPPLIER_DOCUMENT))
                .thenReturn(false);

        new CustomerDevSeeder(customerRepository, supplierRepository, contactPersonRepository, companyFacade).run(null);

        verify(customerRepository, never()).save(any());
        verify(contactPersonRepository, never()).save(any());
        ArgumentCaptor<Supplier> supplierCaptor = ArgumentCaptor.forClass(Supplier.class);
        verify(supplierRepository).save(supplierCaptor.capture());
        assertThat(supplierCaptor.getValue().getCompanyId()).isEqualTo(ACME_ID);
        assertThat(supplierCaptor.getValue().getDocument()).isEqualTo(CustomerDevSeeder.SUPPLIER_DOCUMENT);
    }

    private static Customer persisted(Customer customer) {
        ReflectionTestUtils.setField(customer, "id", UUID.randomUUID());
        return customer;
    }

    private static CompanySnapshot company(UUID id) {
        return new CompanySnapshot(id, null, null, null, null, null, null, null, null, null);
    }

    private static Address address(String number) {
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
