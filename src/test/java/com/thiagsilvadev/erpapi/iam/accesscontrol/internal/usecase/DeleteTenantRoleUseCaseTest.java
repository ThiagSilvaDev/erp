package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import com.thiagsilvadev.erpapi.common.domain.exception.ConflictException;
import com.thiagsilvadev.erpapi.common.security.AuditActorResolver;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.AuthorizationStatus;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.TenantRoleDeletedEvent;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.TenantRole;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.RolePermissionRepository;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.TenantRoleAssignmentRepository;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.TenantRoleRepository;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.service.RoleTemplateService;

class DeleteTenantRoleUseCaseTest {

    private final TenantRoleRepository roleRepository = mock(TenantRoleRepository.class);
    private final RolePermissionRepository permissionRepository = mock(RolePermissionRepository.class);
    private final TenantRoleAssignmentRepository assignmentRepository = mock(TenantRoleAssignmentRepository.class);
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
    private final DeleteTenantRoleUseCase useCase = new DeleteTenantRoleUseCase(
            roleRepository, permissionRepository, assignmentRepository, eventPublisher, mock(AuditActorResolver.class));

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldDeactivateUnassignedCustomRole() {
        UUID companyId = UUID.randomUUID();
        TenantContext.setTenantId(companyId);
        TenantRole role = customRole(companyId);
        when(roleRepository.findByIdAndCompanyId(role.getId(), companyId)).thenReturn(Optional.of(role));
        when(assignmentRepository.existsByRoleId(role.getId())).thenReturn(false);
        when(permissionRepository.findByRoleId(role.getId())).thenReturn(List.of());

        useCase.execute(role.getId());

        assertThat(role.getStatus()).isEqualTo(AuthorizationStatus.INACTIVE);
        TenantRoleDeletedEvent event = publishedEvent(TenantRoleDeletedEvent.class);
        assertThat(event.companyId()).isEqualTo(companyId);
        assertThat(event.roleId()).isEqualTo(role.getId());
        assertThat(event.permissions()).isEmpty();
    }

    @Test
    void shouldRejectDeleteOfAssignedCustomRole() {
        UUID companyId = UUID.randomUUID();
        TenantContext.setTenantId(companyId);
        TenantRole role = customRole(companyId);
        when(roleRepository.findByIdAndCompanyId(role.getId(), companyId)).thenReturn(Optional.of(role));
        when(assignmentRepository.existsByRoleId(role.getId())).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(role.getId()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Assigned roles cannot be deleted");
    }

    @Test
    void shouldRejectDeleteOnBuiltInRole() {
        UUID companyId = UUID.randomUUID();
        TenantContext.setTenantId(companyId);
        TenantRole role = builtInRole(companyId, RoleTemplateService.ADMIN);
        when(roleRepository.findByIdAndCompanyId(role.getId(), companyId)).thenReturn(Optional.of(role));
        when(assignmentRepository.existsByRoleId(role.getId())).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(role.getId()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Built-in roles cannot be deleted");
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
}
