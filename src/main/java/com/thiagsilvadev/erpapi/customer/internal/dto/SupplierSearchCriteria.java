package com.thiagsilvadev.erpapi.customer.internal.dto;

import java.util.UUID;

public record SupplierSearchCriteria(String name, String document, UUID companyId) {}
