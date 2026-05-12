package com.thiagsilvadev.erpapi.company.api;

import java.util.UUID;

import com.thiagsilvadev.erpapi.common.model.Cnpj;
import com.thiagsilvadev.erpapi.common.model.Contact;
import com.thiagsilvadev.erpapi.common.model.address.Address;

public record CompanySnapshot(
        UUID id,
        String legalName,
        String tradeName,
        Cnpj cnpj,
        CompanyStatus status,
        CompanyType type,
        Contact contact,
        Address address) {}
