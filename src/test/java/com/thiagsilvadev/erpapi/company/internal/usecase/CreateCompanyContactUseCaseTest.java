package com.thiagsilvadev.erpapi.company.internal.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.domain.model.Cnpj;
import com.thiagsilvadev.erpapi.common.domain.model.Contact;
import com.thiagsilvadev.erpapi.common.domain.model.address.Address;
import com.thiagsilvadev.erpapi.common.domain.model.address.Country;
import com.thiagsilvadev.erpapi.company.api.CompanyType;
import com.thiagsilvadev.erpapi.company.internal.dto.CompanyContactResponse;
import com.thiagsilvadev.erpapi.company.internal.dto.ContactRequest;
import com.thiagsilvadev.erpapi.company.internal.dto.ContactResponse;
import com.thiagsilvadev.erpapi.company.internal.dto.CreateCompanyContactRequest;
import com.thiagsilvadev.erpapi.company.internal.mapper.CompanyMapper;
import com.thiagsilvadev.erpapi.company.internal.model.Company;
import com.thiagsilvadev.erpapi.company.internal.model.CompanyContact;
import com.thiagsilvadev.erpapi.company.internal.repository.CompanyContactRepository;
import com.thiagsilvadev.erpapi.company.internal.repository.CompanyRepository;

@ExtendWith(MockitoExtension.class)
class CreateCompanyContactUseCaseTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private CompanyContactRepository companyContactRepository;

    @Mock
    private CompanyMapper companyMapper;

    @Test
    void shouldCreateFirstContactAsPrimary() {
        UUID companyId = UUID.randomUUID();
        CreateCompanyContactRequest request = request(companyId, false);
        Contact contact = Contact.create("buyer@example.com", "11912345678");
        CompanyContactResponse response = response(companyId, true);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company()));
        when(companyContactRepository.existsByCompanyId(companyId)).thenReturn(false);
        when(companyMapper.toContact(request.contact())).thenReturn(contact);
        when(companyContactRepository.save(any(CompanyContact.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(companyMapper.toResponse(any(CompanyContact.class))).thenReturn(response);

        CompanyContactResponse created = useCase().execute(request);

        ArgumentCaptor<CompanyContact> captor = ArgumentCaptor.forClass(CompanyContact.class);
        verify(companyContactRepository).save(captor.capture());
        assertThat(captor.getValue().isPrimaryContact()).isTrue();
        assertThat(created.primaryContact()).isTrue();
    }

    @Test
    void shouldRejectMissingCompany() {
        UUID companyId = UUID.randomUUID();
        CreateCompanyContactRequest request = request(companyId, false);
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase().execute(request)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldDemoteExistingPrimaryWhenCreatingNewPrimary() {
        UUID companyId = UUID.randomUUID();
        CreateCompanyContactRequest request = request(companyId, true);
        CompanyContact previousPrimary = CompanyContact.create(
                companyId, "Old Primary", "manager", Contact.create("old@example.com", null), true);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company()));
        when(companyContactRepository.existsByCompanyId(companyId)).thenReturn(true);
        when(companyContactRepository.findByCompanyIdAndPrimaryContactTrue(companyId))
                .thenReturn(Optional.of(previousPrimary));
        when(companyMapper.toContact(request.contact())).thenReturn(Contact.create("buyer@example.com", "11912345678"));
        when(companyContactRepository.save(any(CompanyContact.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(companyMapper.toResponse(any(CompanyContact.class))).thenReturn(response(companyId, true));

        useCase().execute(request);

        assertThat(previousPrimary.isPrimaryContact()).isFalse();
    }

    private CreateCompanyContactUseCase useCase() {
        return new CreateCompanyContactUseCase(companyRepository, companyContactRepository, companyMapper);
    }

    private static CreateCompanyContactRequest request(UUID companyId, boolean primaryContact) {
        return new CreateCompanyContactRequest(
                companyId,
                "Maria Silva",
                "buyer",
                new ContactRequest("buyer@example.com", "11912345678"),
                primaryContact);
    }

    private static CompanyContactResponse response(UUID companyId, boolean primaryContact) {
        return new CompanyContactResponse(
                UUID.randomUUID(),
                companyId,
                "Maria Silva",
                "buyer",
                new ContactResponse("buyer@example.com", "11912345678"),
                primaryContact);
    }

    private static Company company() {
        return Company.create(
                1L,
                "Company",
                "Trade",
                Cnpj.create("11222333000181"),
                CompanyType.FACTORY,
                Address.builder()
                        .postalCode("01001000")
                        .street("Street")
                        .number("10")
                        .district("District")
                        .city("City")
                        .region("SP")
                        .country(Country.BR)
                        .build(),
                Contact.create("company@example.com", null));
    }
}
