package com.thiagsilvadev.erpapi.customer.internal.dto;

import java.util.UUID;

public record SupplierResponse(
        UUID id, String name, String document, UUID companyId, ContactResponse contact, AddressResponse address) {}
