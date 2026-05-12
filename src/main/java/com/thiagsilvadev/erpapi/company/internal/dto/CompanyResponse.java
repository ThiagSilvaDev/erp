package com.thiagsilvadev.erpapi.company.internal.dto;

import java.util.UUID;

import com.thiagsilvadev.erpapi.common.model.Cnpj;
import com.thiagsilvadev.erpapi.company.api.CompanyStatus;
import com.thiagsilvadev.erpapi.company.api.CompanyType;

public record CompanyResponse(
        UUID id,
        String legalName,
        String tradeName,
        Cnpj cnpj,
        CompanyStatus status,
        CompanyType type,
        ContactResponse contact,
        AddressResponse address) {}
