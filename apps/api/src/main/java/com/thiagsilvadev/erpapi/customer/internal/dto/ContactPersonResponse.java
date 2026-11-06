package com.thiagsilvadev.erpapi.customer.internal.dto;

import java.util.UUID;

public record ContactPersonResponse(UUID id, String name, UUID customerId, ContactResponse contact) {}
