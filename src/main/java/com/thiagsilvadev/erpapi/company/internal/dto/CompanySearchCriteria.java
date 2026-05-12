package com.thiagsilvadev.erpapi.company.internal.dto;

import com.thiagsilvadev.erpapi.company.api.CompanyStatus;
import com.thiagsilvadev.erpapi.company.api.CompanyType;

public record CompanySearchCriteria(CompanyType type, CompanyStatus status, String cnpj, String name) {}
