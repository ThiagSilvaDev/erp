package com.thiagsilvadev.erpapi.customer.internal.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.thiagsilvadev.erpapi.common.domain.exception.ConflictException;
import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.domain.model.Contact;
import com.thiagsilvadev.erpapi.common.domain.model.address.Address;
import com.thiagsilvadev.erpapi.common.domain.model.address.Country;
import com.thiagsilvadev.erpapi.company.api.CompanyFacade;
import com.thiagsilvadev.erpapi.customer.internal.dto.CreateCustomerRequest;
import com.thiagsilvadev.erpapi.customer.internal.dto.CustomerMapper;
import com.thiagsilvadev.erpapi.customer.internal.model.Customer;
import com.thiagsilvadev.erpapi.customer.internal.repository.CustomerRepository;

@ExtendWith(MockitoExtension.class)
class CreateCustomerUseCaseTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CompanyFacade companyFacade;

    @Mock
    private CustomerMapper mapper;

    @Test
    void shouldRejectMissingCompany() {
        CreateCustomerRequest request = customerRequest();
        when(companyFacade.findById(request.companyId())).thenReturn(Optional.empty());

        CreateCustomerUseCase useCase = new CreateCustomerUseCase(customerRepository, companyFacade, mapper);

        assertThatThrownBy(() -> useCase.execute(request)).isInstanceOf(ResourceNotFoundException.class);

        verify(customerRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldRejectDuplicatedDocument() {
        CreateCustomerRequest request = customerRequest();
        when(companyFacade.findById(request.companyId())).thenReturn(Optional.of(org.mockito.Mockito.mock()));
        when(customerRepository.existsByDocument("12345678900")).thenReturn(true);

        CreateCustomerUseCase useCase = new CreateCustomerUseCase(customerRepository, companyFacade, mapper);

        assertThatThrownBy(() -> useCase.execute(request)).isInstanceOf(ConflictException.class);

        verify(customerRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldCreateCustomer() {
        CreateCustomerRequest request = customerRequest();
        Contact contact = Contact.create("customer@example.com", "11912345678");
        Address address = address();
        when(companyFacade.findById(request.companyId())).thenReturn(Optional.of(org.mockito.Mockito.mock()));
        when(customerRepository.existsByDocument("12345678900")).thenReturn(false);
        when(mapper.toContact(request.contact())).thenReturn(contact);
        when(mapper.toAddress(request.address())).thenReturn(address);
        when(customerRepository.save(org.mockito.ArgumentMatchers.any(Customer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateCustomerUseCase useCase = new CreateCustomerUseCase(customerRepository, companyFacade, mapper);

        useCase.execute(request);

        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(customerCaptor.capture());
    }

    private static CreateCustomerRequest customerRequest() {
        return new CreateCustomerRequest(
                "Customer",
                "123.456.789-00",
                UUID.randomUUID(),
                new com.thiagsilvadev.erpapi.customer.internal.dto.ContactRequest(
                        "customer@example.com", "11912345678"),
                new com.thiagsilvadev.erpapi.customer.internal.dto.AddressRequest(
                        "01001000", "Street", "10", null, "District", "City", "SP", "BR"));
    }

    private static Address address() {
        return Address.builder()
                .postalCode("01001000")
                .street("Street")
                .number("10")
                .district("District")
                .city("City")
                .region("SP")
                .country(Country.BR)
                .build();
    }
}
