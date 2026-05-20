package com.thiagsilvadev.erpapi.company.internal.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.thiagsilvadev.erpapi.common.domain.model.Contact;
import com.thiagsilvadev.erpapi.company.internal.dto.CompanyContactResponse;
import com.thiagsilvadev.erpapi.company.internal.dto.ContactResponse;
import com.thiagsilvadev.erpapi.company.internal.mapper.CompanyMapper;
import com.thiagsilvadev.erpapi.company.internal.model.CompanyContact;
import com.thiagsilvadev.erpapi.company.internal.repository.CompanyContactRepository;

@ExtendWith(MockitoExtension.class)
class MarkCompanyContactPrimaryUseCaseTest {

    @Mock
    private CompanyContactRepository companyContactRepository;

    @Mock
    private CompanyMapper companyMapper;

    @Test
    void shouldDemoteOldPrimaryAndMarkRequestedContactPrimary() {
        UUID companyId = UUID.randomUUID();
        UUID contactId = UUID.randomUUID();
        CompanyContact requested = CompanyContact.create(
                companyId, "New Primary", "owner", Contact.create("new@example.com", null), false);
        CompanyContact previous = CompanyContact.create(
                companyId, "Old Primary", "manager", Contact.create("old@example.com", null), true);
        org.springframework.test.util.ReflectionTestUtils.setField(requested, "id", contactId);
        org.springframework.test.util.ReflectionTestUtils.setField(previous, "id", UUID.randomUUID());

        when(companyContactRepository.findById(contactId)).thenReturn(Optional.of(requested));
        when(companyContactRepository.findByCompanyIdAndPrimaryContactTrue(companyId))
                .thenReturn(Optional.of(previous));
        when(companyMapper.toResponse(requested))
                .thenReturn(new CompanyContactResponse(
                        contactId,
                        companyId,
                        "New Primary",
                        "owner",
                        new ContactResponse("new@example.com", null),
                        true));

        CompanyContactResponse response =
                new MarkCompanyContactPrimaryUseCase(companyContactRepository, companyMapper).execute(contactId);

        assertThat(previous.isPrimaryContact()).isFalse();
        assertThat(requested.isPrimaryContact()).isTrue();
        assertThat(response.primaryContact()).isTrue();
    }
}
