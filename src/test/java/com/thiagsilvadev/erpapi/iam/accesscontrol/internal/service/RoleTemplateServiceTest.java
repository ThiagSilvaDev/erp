package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import com.thiagsilvadev.erpapi.common.security.AuditActorResolver;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.IamPermissions;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.TenantRoleCreatedEvent;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.RolePermission;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.TenantRole;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.TenantRoleType;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.RolePermissionRepository;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.TenantRoleRepository;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.security.PermissionCatalog;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.security.PermissionCatalogFixture;
import com.thiagsilvadev.erpapi.logistics.api.LogisticsPermissions;

class RoleTemplateServiceTest {

    private final TenantRoleRepository roleRepository = mock(TenantRoleRepository.class);
    private final RolePermissionRepository permissionRepository = mock(RolePermissionRepository.class);
    private final PermissionCatalog permissionCatalog = PermissionCatalogFixture.catalog();
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
    private final RoleTemplateService service = new RoleTemplateService(
            roleRepository,
            permissionRepository,
            permissionCatalog,
            PermissionCatalogFixture.providers(),
            eventPublisher,
            mock(AuditActorResolver.class));

    @Test
    void shouldSeedBuiltInRolesWithDefaultPermissions() {
        UUID companyId = UUID.randomUUID();
        when(roleRepository.findByCompanyIdAndCode(eq(companyId), anyString())).thenReturn(Optional.empty());
        when(roleRepository.saveAndFlush(any(TenantRole.class))).thenAnswer(invocation -> {
            TenantRole role = invocation.getArgument(0);
            ReflectionTestUtils.setField(role, "id", UUID.randomUUID());
            return role;
        });

        service.ensureDefaults(companyId);

        List<TenantRole> roles = savedRoles();
        assertThat(roles)
                .extracting(TenantRole::getCode)
                .containsExactly(
                        RoleTemplateService.OWNER,
                        RoleTemplateService.ADMIN,
                        RoleTemplateService.MANAGER,
                        RoleTemplateService.OPERATOR,
                        RoleTemplateService.DRIVER);
        assertThat(roles).allMatch(role -> role.getType() == TenantRoleType.BUILT_IN);

        List<Set<String>> permissions = savedPermissionSets();
        assertThat(permissions.get(0)).isEqualTo(permissionCatalog.tenantPermissions());
        assertThat(permissions.get(1))
                .isEqualTo(permissionCatalog.tenantPermissions().stream()
                        .filter(permission -> !permission.equals(IamPermissions.OWNERSHIP_TRANSFER))
                        .collect(Collectors.toSet()));
        assertThat(permissions.get(4)).containsExactly(LogisticsPermissions.DRIVER_WORKFLOW);

        verify(eventPublisher, times(5)).publishEvent(any(TenantRoleCreatedEvent.class));
    }

    @Test
    void shouldNotRecreateExistingBuiltInRoles() {
        UUID companyId = UUID.randomUUID();
        TenantRole existing = TenantRole.builtIn(companyId, RoleTemplateService.OWNER, "Owner", null);
        when(roleRepository.findByCompanyIdAndCode(eq(companyId), anyString())).thenReturn(Optional.of(existing));

        service.ensureDefaults(companyId);

        verify(roleRepository, never()).saveAndFlush(any());
        verifyNoInteractions(permissionRepository);
        verifyNoInteractions(eventPublisher);
    }

    private List<TenantRole> savedRoles() {
        ArgumentCaptor<TenantRole> captor = ArgumentCaptor.forClass(TenantRole.class);
        verify(roleRepository, times(5)).saveAndFlush(captor.capture());
        return captor.getAllValues();
    }

    @SuppressWarnings("unchecked")
    private List<Set<String>> savedPermissionSets() {
        ArgumentCaptor<List<RolePermission>> captor = ArgumentCaptor.forClass(List.class);
        verify(permissionRepository, times(5)).saveAll(captor.capture());
        return captor.getAllValues().stream()
                .map(permissions -> permissions.stream()
                        .map(RolePermission::getPermissionKey)
                        .collect(Collectors.toSet()))
                .toList();
    }
}
