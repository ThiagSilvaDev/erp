package com.thiagsilvadev.erpapi.customer.internal.dto;

import java.util.UUID;

public record CustomerResponse(
        UUID id, String name, String document, UUID ownerCompanyId, ContactResponse contact, AddressResponse address) {}
