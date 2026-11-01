package com.thiagsilvadev.erpapi.catalog.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.thiagsilvadev.erpapi.common.domain.exception.DomainValidationException;

class CategorySkuGenerationPolicyTest {
    private final CategorySkuGenerationPolicy policy = new CategorySkuGenerationPolicy();

    @Test
    void shouldPadAndAllowLargerNumbers() {
        var context = new SkuGenerationContext(UUID.randomUUID(), "OFFICE");
        assertThat(policy.generate(context, 1).getValue()).isEqualTo("OFFICE-00000001");
        assertThat(policy.generate(context, 100000000).getValue()).isEqualTo("OFFICE-100000000");
    }

    @Test
    void shouldRejectOverlongGeneratedSku() {
        var context = new SkuGenerationContext(UUID.randomUUID(), "X".repeat(80));
        assertThatThrownBy(() -> policy.generate(context, 1)).isInstanceOf(DomainValidationException.class);
    }
}
