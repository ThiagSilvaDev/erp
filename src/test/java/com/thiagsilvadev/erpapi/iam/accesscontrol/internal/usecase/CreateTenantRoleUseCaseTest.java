package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import com.thiagsilvadev.erpapi.catalog.api.CatalogPermissions;
import com.thiagsilvadev.erpapi.common.security.AuditActorResolver;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.TenantRoleCreatedEvent;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.dto.CreateTenantRoleRequest;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.dto.TenantRoleResponse;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.mapper.TenantRoleMapper;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.RolePermission;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.TenantRole;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.TenantRoleType;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.RolePermissionRepository;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.TenantRoleRepository;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.security.PermissionCatalogFixture;

class CreateTenantRoleUseCaseTest {

    private final TenantRoleRepository roleRepository = mock(TenantRoleRepository.class);
    private final RolePermissionRepository permissionRepository = mock(RolePermissionRepository.class);
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
    private final CreateTenantRoleUseCase useCase = new CreateTenantRoleUseCase(
            roleRepository,
            permissionRepository,
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
    void shouldCreateCustomRoleWithCustomPermissions() {
        UUID companyId = UUID.randomUUID();
        TenantContext.setTenantId(companyId);
        authenticate(CatalogPermissions.READ);
        when(roleRepository.saveAndFlush(any(TenantRole.class))).thenAnswer(invocation -> {
            TenantRole role = invocation.getArgument(0);
            ReflectionTestUtils.setField(role, "id", UUID.randomUUID());
            return role;
        });
        when(permissionRepository.findByRoleId(any())).thenReturn(List.of());

        TenantRoleResponse response = useCase.execute(
                new CreateTenantRoleRequest("Analyst", "Read-only analyst", Set.of(CatalogPermissions.READ)));

        assertThat(response.type()).isEqualTo(TenantRoleType.CUSTOM);
        assertThat(response.name()).isEqualTo("Analyst");
        assertThat(savedPermissionKeys()).containsExactlyInAnyOrder(CatalogPermissions.READ);
        TenantRoleCreatedEvent event = publishedEvent(TenantRoleCreatedEvent.class);
        assertThat(event.roleId()).isEqualTo(response.id());
        assertThat(event.roleCode()).isEqualTo(response.code());
        assertThat(event.permissions()).containsExactly(CatalogPermissions.READ);
    }

    @Test
    void shouldRejectUnknownPermissions() {
        TenantContext.setTenantId(UUID.randomUUID());
        authenticate(CatalogPermissions.READ);

        assertThatThrownBy(() ->
                        useCase.execute(new CreateTenantRoleRequest("Analyst", null, Set.of("unknown:permission"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown or non-tenant permissions");
    }

    @Test
    void shouldRejectPermissionsTheCallerDoesNotPossess() {
        TenantContext.setTenantId(UUID.randomUUID());
        authenticate(CatalogPermissions.READ);

        assertThatThrownBy(() -> useCase.execute(new CreateTenantRoleRequest(
                        "Analyst", null, Set.of(CatalogPermissions.READ, CatalogPermissions.WRITE))))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("caller does not possess");
    }

    private static void authenticate(String... authorities) {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("user", null, authorities));
    }

    private <T> T publishedEvent(Class<T> eventType) {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(captor.capture());
        return eventType.cast(captor.getValue());
    }

    @SuppressWarnings("unchecked")
    private Set<String> savedPermissionKeys() {
        ArgumentCaptor<List<RolePermission>> captor = ArgumentCaptor.forClass(List.class);
        verify(permissionRepository).saveAll(captor.capture());
        return captor.getValue().stream().map(RolePermission::getPermissionKey).collect(Collectors.toSet());
    }
}
