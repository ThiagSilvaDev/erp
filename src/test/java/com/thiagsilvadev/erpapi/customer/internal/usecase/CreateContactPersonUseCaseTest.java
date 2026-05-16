package com.thiagsilvadev.erpapi.customer.internal.usecase;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.thiagsilvadev.erpapi.common.domain.model.Contact;
import com.thiagsilvadev.erpapi.customer.internal.dto.ContactRequest;
import com.thiagsilvadev.erpapi.customer.internal.dto.CreateContactPersonRequest;
import com.thiagsilvadev.erpapi.customer.internal.dto.CustomerMapper;
import com.thiagsilvadev.erpapi.customer.internal.model.ContactPerson;
import com.thiagsilvadev.erpapi.customer.internal.model.Customer;
import com.thiagsilvadev.erpapi.customer.internal.repository.ContactPersonRepository;
import com.thiagsilvadev.erpapi.customer.internal.repository.CustomerRepository;

@ExtendWith(MockitoExtension.class)
class CreateContactPersonUseCaseTest {

    @Mock
    private ContactPersonRepository contactPersonRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper mapper;

    @Test
    void shouldCreateContactPerson() {
        CreateContactPersonRequest request = new CreateContactPersonRequest(
                "Contact", UUID.randomUUID(), new ContactRequest("contact@example.com", "11912345678"));
        when(customerRepository.findById(request.customerId()))
                .thenReturn(Optional.of(org.mockito.Mockito.mock(Customer.class)));
        when(mapper.toContact(request.contact())).thenReturn(Contact.create("contact@example.com", "11912345678"));
        when(contactPersonRepository.save(org.mockito.ArgumentMatchers.any(ContactPerson.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateContactPersonUseCase useCase =
                new CreateContactPersonUseCase(contactPersonRepository, customerRepository, mapper);

        useCase.execute(request);

        ArgumentCaptor<ContactPerson> contactPersonCaptor = ArgumentCaptor.forClass(ContactPerson.class);
        verify(contactPersonRepository).save(contactPersonCaptor.capture());
    }
}
