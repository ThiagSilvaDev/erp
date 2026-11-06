package com.thiagsilvadev.erpapi.company.api;

import java.util.UUID;

import com.thiagsilvadev.erpapi.common.domain.model.Cnpj;
import com.thiagsilvadev.erpapi.common.domain.model.Contact;
import com.thiagsilvadev.erpapi.common.domain.model.address.Address;

public record CompanySnapshot(
        UUID id,
        Long companyCode,
        String legalName,
        String tradeName,
        Cnpj cnpj,
        CompanyStatus status,
        CompanyType type,
        Contact contact,
        Address address,
        CompanyContactSnapshot primaryContact) {}
