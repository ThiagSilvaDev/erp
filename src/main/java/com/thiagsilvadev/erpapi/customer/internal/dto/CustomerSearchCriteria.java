package com.thiagsilvadev.erpapi.customer.internal.dto;

import java.util.UUID;

public record CustomerSearchCriteria(String name, String document, UUID companyId) {}
