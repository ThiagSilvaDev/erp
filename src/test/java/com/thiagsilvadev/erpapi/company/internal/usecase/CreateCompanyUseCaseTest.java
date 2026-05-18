package com.thiagsilvadev.erpapi.company.internal.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.thiagsilvadev.erpapi.common.domain.model.Cnpj;
import com.thiagsilvadev.erpapi.common.domain.model.Contact;
import com.thiagsilvadev.erpapi.common.domain.model.address.Address;
import com.thiagsilvadev.erpapi.common.domain.model.address.Country;
import com.thiagsilvadev.erpapi.common.domain.numbering.BusinessNumberService;
import com.thiagsilvadev.erpapi.common.domain.numbering.BusinessNumberType;
import com.thiagsilvadev.erpapi.company.api.CompanyType;
import com.thiagsilvadev.erpapi.company.internal.dto.AddressRequest;
import com.thiagsilvadev.erpapi.company.internal.dto.ContactRequest;
import com.thiagsilvadev.erpapi.company.internal.dto.CreateCompanyRequest;
import com.thiagsilvadev.erpapi.company.internal.event.CompanyEventPublisher;
import com.thiagsilvadev.erpapi.company.internal.exception.CnpjAlreadyExistsException;
import com.thiagsilvadev.erpapi.company.internal.mapper.CompanyMapper;
import com.thiagsilvadev.erpapi.company.internal.model.Company;
import com.thiagsilvadev.erpapi.company.internal.repository.CompanyRepository;

@ExtendWith(MockitoExtension.class)
class CreateCompanyUseCaseTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private CompanyMapper companyMapper;

    @Mock
    private CompanyEventPublisher eventPublisher;

    @Mock
    private BusinessNumberService businessNumberService;

    @Test
    void shouldRejectDuplicatedCnpj() {
        CreateCompanyRequest request = companyRequest();
        Cnpj cnpj = Cnpj.create(request.cnpj());
        when(companyMapper.toCnpj(request.cnpj())).thenReturn(cnpj);
        when(companyRepository.existsByCnpj(cnpj)).thenReturn(true);

        CreateCompanyUseCase useCase =
                new CreateCompanyUseCase(companyRepository, companyMapper, eventPublisher, businessNumberService);

        assertThatThrownBy(() -> useCase.execute(request)).isInstanceOf(CnpjAlreadyExistsException.class);

        verify(companyRepository, never()).saveAndFlush(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldTranslateDuplicatedCnpjRace() {
        CreateCompanyRequest request = companyRequest();
        Cnpj cnpj = Cnpj.create(request.cnpj());
        when(companyMapper.toCnpj(request.cnpj())).thenReturn(cnpj);
        when(companyRepository.existsByCnpj(cnpj)).thenReturn(false);
        when(companyMapper.trimToNull(request.legalName())).thenReturn(request.legalName());
        when(companyMapper.trimToNull(request.tradeName())).thenReturn(request.tradeName());
        when(companyMapper.toAddress(request.address())).thenReturn(address());
        when(companyMapper.toContact(request.contact())).thenReturn(Contact.create("company@example.com", null));
        when(businessNumberService.next(BusinessNumberType.COMPANY)).thenReturn(1L);
        when(companyRepository.saveAndFlush(org.mockito.ArgumentMatchers.any(Company.class)))
                .thenThrow(new DataIntegrityViolationException("uk_company_cnpj"));

        CreateCompanyUseCase useCase =
                new CreateCompanyUseCase(companyRepository, companyMapper, eventPublisher, businessNumberService);

        assertThatThrownBy(() -> useCase.execute(request)).isInstanceOf(CnpjAlreadyExistsException.class);

        verify(eventPublisher, never()).companyCreated(org.mockito.ArgumentMatchers.any());
    }

    private static CreateCompanyRequest companyRequest() {
        return new CreateCompanyRequest(
                "Company",
                "Trade",
                "11222333000181",
                CompanyType.FACTORY,
                new ContactRequest("company@example.com", null),
                new AddressRequest("01001000", "Street", "10", null, "District", "City", "SP", "BR"));
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
