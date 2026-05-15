package com.thiagsilvadev.erpapi.customer.api;

import java.util.UUID;

public record CustomerSnapshot(UUID id, String name, String document, UUID companyId) {}
