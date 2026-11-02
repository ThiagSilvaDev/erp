package com.thiagsilvadev.erpapi.company.internal.dto;

import jakarta.validation.constraints.Size;

import com.thiagsilvadev.erpapi.company.api.CompanyStatus;
import com.thiagsilvadev.erpapi.company.api.CompanyType;

public record CompanySearchCriteria(
        CompanyType type, CompanyStatus status, @Size(max = 24) String cnpj, @Size(max = 160) String name) {}
