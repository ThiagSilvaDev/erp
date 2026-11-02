package com.thiagsilvadev.erpapi.customer.internal.dto;

import java.util.UUID;

import jakarta.validation.constraints.Size;

public record SupplierSearchCriteria(@Size(max = 120) String name, @Size(max = 32) String document, UUID companyId) {}
