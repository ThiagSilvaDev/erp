package com.thiagsilvadev.erpapi.catalog.internal.dto;

import java.math.BigDecimal;

public record MoneyResponse(BigDecimal amount, String currencyCode) {}
