package com.thiagsilvadev.erpapi.common.tenant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class TenantContextTest {

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldStoreCurrentTenantId() {
        UUID tenantId = UUID.randomUUID();

        TenantContext.setTenantId(tenantId);

        assertThat(TenantContext.getTenantId()).contains(tenantId);
        assertThat(TenantContext.requireTenantId()).isEqualTo(tenantId);
    }

    @Test
    void shouldClearCurrentTenantId() {
        TenantContext.setTenantId(UUID.randomUUID());

        TenantContext.clear();

        assertThat(TenantContext.getTenantId()).isEmpty();
    }

    @Test
    void shouldRejectMissingTenantId() {
        assertThatThrownBy(TenantContext::requireTenantId)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Tenant context is not available.");
    }
}
