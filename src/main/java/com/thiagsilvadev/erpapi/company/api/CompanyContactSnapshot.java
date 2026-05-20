package com.thiagsilvadev.erpapi.company.api;

import java.util.UUID;

import com.thiagsilvadev.erpapi.common.domain.model.Contact;

public record CompanyContactSnapshot(
        UUID id, UUID companyId, String name, String role, Contact contact, boolean primaryContact) {}
