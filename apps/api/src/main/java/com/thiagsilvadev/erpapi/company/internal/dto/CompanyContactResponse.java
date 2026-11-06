package com.thiagsilvadev.erpapi.company.internal.dto;

import java.util.UUID;

public record CompanyContactResponse(
        UUID id, UUID companyId, String name, String role, ContactResponse contact, boolean primaryContact) {}
