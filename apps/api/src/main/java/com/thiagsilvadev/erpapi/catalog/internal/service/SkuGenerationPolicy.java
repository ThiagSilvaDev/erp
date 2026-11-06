package com.thiagsilvadev.erpapi.catalog.internal.service;

import com.thiagsilvadev.erpapi.catalog.internal.model.Sku;

public interface SkuGenerationPolicy {
    Sku generate(SkuGenerationContext context, long number);
}
