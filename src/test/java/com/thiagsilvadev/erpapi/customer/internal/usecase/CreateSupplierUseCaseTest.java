package com.thiagsilvadev.erpapi.customer.internal.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.thiagsilvadev.erpapi.common.domain.exception.ConflictException;
import com.thiagsilvadev.erpapi.common.domain.model.Contact;
import com.thiagsilvadev.erpapi.common.domain.model.address.Address;
import com.thiagsilvadev.erpapi.common.domain.model.address.Country;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.company.api.CompanyFacade;
import com.thiagsilvadev.erpapi.customer.internal.dto.AddressRequest;
import com.thiagsilvadev.erpapi.customer.internal.dto.ContactRequest;
import com.thiagsilvadev.erpapi.customer.internal.dto.CreateSupplierRequest;
import com.thiagsilvadev.erpapi.customer.internal.dto.CustomerMapper;
import com.thiagsilvadev.erpapi.customer.internal.model.Supplier;
import com.thiagsilvadev.erpapi.customer.internal.repository.SupplierRepository;

@ExtendWith(MockitoExtension.class)
class CreateSupplierUseCaseTest {
    private static final UUID TENANT_ID = UUID.randomUUID();

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private CompanyFacade companyFacade;

    @Mock
    private CustomerMapper mapper;

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldCreateSupplier() {
        CreateSupplierRequest request = supplierRequest();
        TenantContext.setTenantId(TENANT_ID);
        when(companyFacade.findById(TENANT_ID)).thenReturn(Optional.of(org.mockito.Mockito.mock()));
        when(supplierRepository.existsByCompanyIdAndDocument(TENANT_ID, "12345678900"))
                .thenReturn(false);
        when(mapper.toContact(request.contact())).thenReturn(Contact.create("supplier@example.com", "11912345678"));
        when(mapper.toAddress(request.address())).thenReturn(address());
        when(supplierRepository.saveAndFlush(org.mockito.ArgumentMatchers.any(Supplier.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateSupplierUseCase useCase = new CreateSupplierUseCase(supplierRepository, companyFacade, mapper);

        useCase.execute(request);

        ArgumentCaptor<Supplier> supplierCaptor = ArgumentCaptor.forClass(Supplier.class);
        verify(supplierRepository).saveAndFlush(supplierCaptor.capture());
        org.assertj.core.api.Assertions.assertThat(supplierCaptor.getValue().getCompanyId())
                .isEqualTo(TENANT_ID);
    }

    @Test
    void shouldTranslateDuplicateDocumentRace() {
        CreateSupplierRequest request = supplierRequest();
        TenantContext.setTenantId(TENANT_ID);
        when(companyFacade.findById(TENANT_ID)).thenReturn(Optional.of(org.mockito.Mockito.mock()));
        when(supplierRepository.existsByCompanyIdAndDocument(TENANT_ID, "12345678900"))
                .thenReturn(false);
        when(mapper.toContact(request.contact())).thenReturn(Contact.create("supplier@example.com", "11912345678"));
        when(mapper.toAddress(request.address())).thenReturn(address());
        when(supplierRepository.saveAndFlush(org.mockito.ArgumentMatchers.any(Supplier.class)))
                .thenThrow(new DataIntegrityViolationException("uk_supplier_document"));

        CreateSupplierUseCase useCase = new CreateSupplierUseCase(supplierRepository, companyFacade, mapper);

        assertThatThrownBy(() -> useCase.execute(request)).isInstanceOf(ConflictException.class);
    }

    private static CreateSupplierRequest supplierRequest() {
        return new CreateSupplierRequest(
                "Supplier",
                "123.456.789-00",
                new ContactRequest("supplier@example.com", "11912345678"),
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
