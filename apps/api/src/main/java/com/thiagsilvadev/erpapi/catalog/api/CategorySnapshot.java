package com.thiagsilvadev.erpapi.catalog.api;

import java.util.UUID;

public record CategorySnapshot(UUID id, String code, String name, String description, CategoryStatus status) {}
