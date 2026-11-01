package com.thiagsilvadev.erpapi.catalog.internal.service;

import java.util.Locale;

import org.springframework.stereotype.Component;

import com.thiagsilvadev.erpapi.catalog.internal.model.Sku;

@Component
public class CategorySkuGenerationPolicy implements SkuGenerationPolicy {
    @Override
    public Sku generate(SkuGenerationContext context, long number) {
        return Sku.of(context.categoryCode() + "-" + String.format(Locale.ROOT, "%08d", number));
    }
}
