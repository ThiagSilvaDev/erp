package com.thiagsilvadev.erpapi.company.internal.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.thiagsilvadev.erpapi.company.api.CompanyStatus;
import com.thiagsilvadev.erpapi.company.api.CompanyType;
import com.thiagsilvadev.erpapi.company.internal.dto.AddressResponse;
import com.thiagsilvadev.erpapi.company.internal.dto.CompanyAddressResponse;
import com.thiagsilvadev.erpapi.company.internal.dto.CompanyResponse;
import com.thiagsilvadev.erpapi.company.internal.dto.CompanySearchCriteria;
import com.thiagsilvadev.erpapi.company.internal.dto.CreateCompanyRequest;
import com.thiagsilvadev.erpapi.company.internal.dto.UpdateCompanyRequest;
import com.thiagsilvadev.erpapi.company.internal.usecase.CompanyQueryUseCase;
import com.thiagsilvadev.erpapi.company.internal.usecase.CreateCompanyUseCase;
import com.thiagsilvadev.erpapi.company.internal.usecase.DeleteCompanyUseCase;
import com.thiagsilvadev.erpapi.company.internal.usecase.GetCompanyAddressUseCase;
import com.thiagsilvadev.erpapi.company.internal.usecase.UpdateCompanyUseCase;

class CompanyControllerTest {

    private CreateCompanyUseCase createCompanyUseCase;
    private UpdateCompanyUseCase updateCompanyUseCase;
    private DeleteCompanyUseCase deleteCompanyUseCase;
    private GetCompanyAddressUseCase getCompanyAddressUseCase;
    private CompanyQueryUseCase companyQueryUseCase;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        createCompanyUseCase = mock(CreateCompanyUseCase.class);
        updateCompanyUseCase = mock(UpdateCompanyUseCase.class);
        deleteCompanyUseCase = mock(DeleteCompanyUseCase.class);
        getCompanyAddressUseCase = mock(GetCompanyAddressUseCase.class);
        companyQueryUseCase = mock(CompanyQueryUseCase.class);
        mockMvc = standaloneSetup(new CompanyController(
                        createCompanyUseCase,
                        updateCompanyUseCase,
                        deleteCompanyUseCase,
                        getCompanyAddressUseCase,
                        companyQueryUseCase))
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void shouldCreateCompany() throws Exception {
        UUID id = UUID.randomUUID();
        when(createCompanyUseCase.execute(any(CreateCompanyRequest.class))).thenReturn(companyResponse(id));

        mockMvc.perform(
                        post("/api/v1/platform/companies")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {
                                  "legalName": "Company",
                                  "tradeName": "Trade",
                                  "cnpj": "11222333000181",
                                  "type": "FACTORY",
                                  "address": {
                                    "postalCode": "01310100",
                                    "street": "Avenida Paulista",
                                    "number": "1000",
                                    "district": "Bela Vista",
                                    "city": "Sao Paulo",
                                    "region": "SP",
                                    "country": "BR"
                                  }
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/platform/companies/" + id))
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void shouldGetCompanyAddressForDeliveryRoutePrefill() throws Exception {
        UUID id = UUID.randomUUID();
        when(getCompanyAddressUseCase.execute(id))
                .thenReturn(new CompanyAddressResponse(
                        id,
                        "Trade",
                        addressResponse(),
                        "Avenida Paulista, 1000 - Bela Vista, Sao Paulo - SP, BR, 01310100"));

        mockMvc.perform(get("/api/v1/platform/companies/{id}/address", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyId").value(id.toString()))
                .andExpect(jsonPath("$.companyName").value("Trade"))
                .andExpect(jsonPath("$.address.street").value("Avenida Paulista"))
                .andExpect(jsonPath("$.deliveryRouteDestinationDescription")
                        .value("Avenida Paulista, 1000 - Bela Vista, Sao Paulo - SP, BR, 01310100"));
    }

    @Test
    void shouldGetCompanyById() throws Exception {
        UUID id = UUID.randomUUID();
        when(companyQueryUseCase.getById(id)).thenReturn(companyResponse(id));

        mockMvc.perform(get("/api/v1/platform/companies/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void shouldListCompanies() throws Exception {
        when(companyQueryUseCase.list(any(CompanySearchCriteria.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        mockMvc.perform(get("/api/v1/platform/companies")).andExpect(status().isOk());
    }

    @Test
    void shouldUpdateCompany() throws Exception {
        UUID id = UUID.randomUUID();
        when(updateCompanyUseCase.execute(eq(id), any(UpdateCompanyRequest.class)))
                .thenReturn(companyResponse(id));

        mockMvc.perform(
                        put("/api/v1/platform/companies/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {
                                  "legalName": "Updated Company",
                                  "tradeName": "Updated Trade",
                                  "type": "CORRESPONDENT",
                                  "address": {
                                    "postalCode": "01310100",
                                    "street": "Avenida Paulista",
                                    "number": "1000",
                                    "district": "Bela Vista",
                                    "city": "Sao Paulo",
                                    "region": "SP",
                                    "country": "BR"
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void shouldDeleteCompany() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/platform/companies/{id}", id)).andExpect(status().isNoContent());
    }

    private static CompanyResponse companyResponse(UUID id) {
        return new CompanyResponse(
                id,
                1L,
                "Company",
                "Trade",
                null,
                CompanyStatus.ACTIVE,
                CompanyType.FACTORY,
                null,
                addressResponse(),
                null);
    }

    private static AddressResponse addressResponse() {
        return new AddressResponse("01310100", "Avenida Paulista", "1000", null, "Bela Vista", "Sao Paulo", "SP", "BR");
    }
}
