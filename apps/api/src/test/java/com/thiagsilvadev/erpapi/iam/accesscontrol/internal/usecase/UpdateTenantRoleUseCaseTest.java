package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import com.thiagsilvadev.erpapi.catalog.api.CatalogPermissions;
import com.thiagsilvadev.erpapi.common.domain.exception.ConflictException;
import com.thiagsilvadev.erpapi.common.security.AuditActorResolver;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.TenantRoleUpdatedEvent;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.dto.TenantRoleResponse;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.dto.UpdateTenantRoleRequest;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.mapper.TenantRoleMapper;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.RolePermission;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.TenantRole;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.RolePermissionRepository;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.TenantMembershipRepository;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.TenantRoleAssignmentRepository;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.TenantRoleRepository;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.security.PermissionCatalogFixture;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.service.RoleTemplateService;

class UpdateTenantRoleUseCaseTest {

    private final TenantRoleRepository roleRepository = mock(TenantRoleRepository.class);
    private final RolePermissionRepository permissionRepository = mock(RolePermissionRepository.class);
    private final TenantRoleAssignmentRepository assignmentRepository = mock(TenantRoleAssignmentRepository.class);
    private final TenantMembershipRepository membershipRepository = mock(TenantMembershipRepository.class);
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
    private final UpdateTenantRoleUseCase useCase = new UpdateTenantRoleUseCase(
            roleRepository,
            permissionRepository,
            assignmentRepository,
            membershipRepository,
            PermissionCatalogFixture.catalog(),
            new TenantRoleMapper(permissionRepository),
            eventPublisher,
            mock(AuditActorResolver.class));

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldUpdateCustomRolePermissions() {
        authenticate(CatalogPermissions.READ);
        UUID companyId = UUID.randomUUID();
        TenantContext.setTenantId(companyId);
        TenantRole role = customRole(companyId);
        when(roleRepository.findByIdAndCompanyId(role.getId(), companyId)).thenReturn(Optional.of(role));
        when(permissionRepository.findByRoleId(role.getId())).thenReturn(List.of());
        when(assignmentRepository.findMembershipIdsByRoleId(role.getId())).thenReturn(List.of());

        TenantRoleResponse response = useCase.execute(
                role.getId(), new UpdateTenantRoleRequest("Senior Analyst", null, Set.of(CatalogPermissions.READ)));

        assertThat(response.name()).isEqualTo("Senior Analyst");
        verify(permissionRepository).deleteByRoleId(role.getId());
        assertThat(savedPermissionKeys()).containsExactlyInAnyOrder(CatalogPermissions.READ);
        TenantRoleUpdatedEvent event = publishedEvent(TenantRoleUpdatedEvent.class);
        assertThat(event.roleId()).isEqualTo(role.getId());
        assertThat(event.beforePermissions()).isEmpty();
        assertThat(event.afterPermissions()).containsExactly(CatalogPermissions.READ);
    }

    @Test
    void shouldRejectUpdateOnBuiltInRole() {
        authenticate(CatalogPermissions.READ);
        UUID companyId = UUID.randomUUID();
        TenantContext.setTenantId(companyId);
        TenantRole role = builtInRole(companyId, RoleTemplateService.OWNER);
        when(roleRepository.findByIdAndCompanyId(role.getId(), companyId)).thenReturn(Optional.of(role));

        assertThatThrownBy(() -> useCase.execute(
                        role.getId(), new UpdateTenantRoleRequest("New name", null, Set.of(CatalogPermissions.READ))))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Built-in roles cannot be edited");

        verifyNoInteractions(permissionRepository, assignmentRepository);
    }

    private static void authenticate(String... authorities) {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("user", null, authorities));
    }

    private <T> T publishedEvent(Class<T> eventType) {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(captor.capture());
        return eventType.cast(captor.getValue());
    }

    private static TenantRole customRole(UUID companyId) {
        TenantRole role = TenantRole.custom(companyId, "Analyst", null);
        ReflectionTestUtils.setField(role, "id", UUID.randomUUID());
        return role;
    }

    private static TenantRole builtInRole(UUID companyId, String code) {
        TenantRole role = TenantRole.builtIn(companyId, code, code, null);
        ReflectionTestUtils.setField(role, "id", UUID.randomUUID());
        return role;
    }

    @SuppressWarnings("unchecked")
    private Set<String> savedPermissionKeys() {
        ArgumentCaptor<List<RolePermission>> captor = ArgumentCaptor.forClass(List.class);
        verify(permissionRepository).saveAll(captor.capture());
        return captor.getValue().stream().map(RolePermission::getPermissionKey).collect(Collectors.toSet());
    }
}
