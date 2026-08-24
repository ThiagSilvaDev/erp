package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.mapper;

import static com.thiagsilvadev.erpapi.iam.accesscontrol.api.AuthorizationStatus.ACTIVE;
import static com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.TenantRoleType.BUILT_IN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.RolePermission;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.TenantRole;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.RolePermissionRepository;

class TenantRoleMapperTest {

    private final RolePermissionRepository permissionRepository = mock(RolePermissionRepository.class);
    private final TenantRoleMapper mapper = new TenantRoleMapper(permissionRepository);

    @Test
    void shouldMapRoleToResponse() {
        UUID companyId = UUID.randomUUID();
        TenantRole role = TenantRole.builtIn(companyId, "MANAGER", "Manager", "Built-in manager role");
        ReflectionTestUtils.setField(role, "id", UUID.randomUUID());
        when(permissionRepository.findByRoleId(role.getId()))
                .thenReturn(List.of(RolePermission.create(role.getId(), "catalog:read")));

        var response = mapper.toResponse(role);

        assertThat(response.id()).isEqualTo(role.getId());
        assertThat(response.code()).isEqualTo("MANAGER");
        assertThat(response.name()).isEqualTo("Manager");
        assertThat(response.description()).isEqualTo("Built-in manager role");
        assertThat(response.type()).isEqualTo(BUILT_IN);
        assertThat(response.status()).isEqualTo(ACTIVE);
        assertThat(response.permissions()).isEqualTo(Set.of("catalog:read"));
    }
}
