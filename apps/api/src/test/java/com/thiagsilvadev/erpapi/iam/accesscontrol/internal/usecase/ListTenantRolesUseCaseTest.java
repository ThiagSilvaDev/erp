package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.usecase;

import static com.thiagsilvadev.erpapi.iam.accesscontrol.api.AuthorizationStatus.ACTIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.dto.TenantRoleResponse;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.mapper.TenantRoleMapper;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.RolePermission;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.TenantRole;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.TenantRoleType;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.RolePermissionRepository;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.TenantRoleRepository;

class ListTenantRolesUseCaseTest {

    private final TenantRoleRepository roleRepository = mock(TenantRoleRepository.class);
    private final RolePermissionRepository permissionRepository = mock(RolePermissionRepository.class);
    private final ListTenantRolesUseCase useCase =
            new ListTenantRolesUseCase(roleRepository, new TenantRoleMapper(permissionRepository));

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldListActiveRolesForCurrentTenantWithoutSideEffects() {
        UUID companyId = UUID.randomUUID();
        TenantContext.setTenantId(companyId);
        TenantRole role = TenantRole.builtIn(companyId, "ADMIN", "Admin", "Built-in admin role");
        ReflectionTestUtils.setField(role, "id", UUID.randomUUID());
        when(roleRepository.findByCompanyIdAndStatusOrderByName(companyId, ACTIVE))
                .thenReturn(List.of(role));
        when(permissionRepository.findByRoleId(role.getId()))
                .thenReturn(List.of(RolePermission.create(role.getId(), "catalog:read")));

        List<TenantRoleResponse> responses = useCase.execute();

        assertThat(responses).hasSize(1);
        TenantRoleResponse response = responses.getFirst();
        assertThat(response.id()).isEqualTo(role.getId());
        assertThat(response.code()).isEqualTo("ADMIN");
        assertThat(response.name()).isEqualTo("Admin");
        assertThat(response.type()).isEqualTo(TenantRoleType.BUILT_IN);
        assertThat(response.status()).isEqualTo(ACTIVE);
        assertThat(response.permissions()).isEqualTo(Set.of("catalog:read"));
    }

    @Test
    void shouldNotWriteWhenNoRolesExist() {
        UUID companyId = UUID.randomUUID();
        TenantContext.setTenantId(companyId);
        when(roleRepository.findByCompanyIdAndStatusOrderByName(companyId, ACTIVE))
                .thenReturn(List.of());

        assertThat(useCase.execute()).isEmpty();
        verifyNoInteractions(permissionRepository);
    }
}
