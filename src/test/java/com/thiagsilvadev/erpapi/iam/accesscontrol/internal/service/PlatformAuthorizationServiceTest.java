package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import com.thiagsilvadev.erpapi.common.security.AuditActorResolver;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.AuthorizationStatus;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.PlatformRoleActivatedEvent;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.PlatformRoleAssignedEvent;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.PlatformRole;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.PlatformRoleAssignment;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.PlatformRoleAssignmentRepository;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.security.PermissionCatalog;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.security.PermissionCatalogFixture;

class PlatformAuthorizationServiceTest {

    private final PlatformRoleAssignmentRepository repository = mock(PlatformRoleAssignmentRepository.class);
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
    private final AuditActorResolver actorResolver = mock(AuditActorResolver.class);
    private final PlatformAuthorizationService service = new PlatformAuthorizationService(
            repository, eventPublisher, PermissionCatalogFixture.catalog(), actorResolver);

    @Test
    void shouldValidateActiveAssignmentWithMatchingVersion() {
        UUID userId = UUID.randomUUID();
        PlatformRoleAssignment assignment = assignment(userId, 4L);
        when(repository.findById(assignment.getId())).thenReturn(Optional.of(assignment));

        var authorization = service.validate(assignment.getId(), userId, 4L);

        assertThat(authorization.assignmentId()).isEqualTo(assignment.getId());
        assertThat(authorization.userId()).isEqualTo(userId);
        assertThat(authorization.authorizationVersion()).isEqualTo(4L);
        assertThat(authorization.role()).isEqualTo(PlatformRole.PLATFORM_ADMIN.name());
        assertThat(authorization.permissions())
                .contains(PermissionCatalog.PLATFORM_COMPANY_READ)
                .contains(PermissionCatalog.PLATFORM_COMPANY_WRITE);
    }

    @Test
    void shouldRejectValidationForUnknownAssignment() {
        UUID assignmentId = UUID.randomUUID();
        when(repository.findById(assignmentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.validate(assignmentId, UUID.randomUUID(), 1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("no longer valid");
    }

    @Test
    void shouldRejectValidationWhenUserDoesNotMatch() {
        UUID userId = UUID.randomUUID();
        PlatformRoleAssignment assignment = assignment(userId, 1L);
        when(repository.findById(assignment.getId())).thenReturn(Optional.of(assignment));

        assertThatThrownBy(() -> service.validate(assignment.getId(), UUID.randomUUID(), 1L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void shouldRejectValidationWhenVersionDiffers() {
        UUID userId = UUID.randomUUID();
        PlatformRoleAssignment assignment = assignment(userId, 5L);
        when(repository.findById(assignment.getId())).thenReturn(Optional.of(assignment));

        assertThatThrownBy(() -> service.validate(assignment.getId(), userId, 4L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void shouldRejectValidationForInactiveAssignment() {
        UUID userId = UUID.randomUUID();
        PlatformRoleAssignment assignment = assignment(userId, 1L);
        ReflectionTestUtils.setField(assignment, "status", AuthorizationStatus.INACTIVE);
        when(repository.findById(assignment.getId())).thenReturn(Optional.of(assignment));

        assertThatThrownBy(() -> service.validate(assignment.getId(), userId, 1L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void shouldRequireActivePlatformAssignment() {
        UUID userId = UUID.randomUUID();
        when(repository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.requireActive(userId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("active platform role");
    }

    @Test
    void shouldReturnActivePlatformSnapshot() {
        UUID userId = UUID.randomUUID();
        PlatformRoleAssignment assignment = assignment(userId, 2L);
        when(repository.findByUserId(userId)).thenReturn(Optional.of(assignment));

        var authorization = service.requireActive(userId);

        assertThat(authorization.assignmentId()).isEqualTo(assignment.getId());
        assertThat(authorization.role()).isEqualTo(PlatformRole.PLATFORM_ADMIN.name());
    }

    @Test
    void shouldCreateAdminAssignmentWhenMissing() {
        UUID userId = UUID.randomUUID();
        when(repository.findByUserId(userId)).thenReturn(Optional.empty());
        when(repository.save(any(PlatformRoleAssignment.class))).thenAnswer(invocation -> {
            PlatformRoleAssignment assignment = invocation.getArgument(0);
            ReflectionTestUtils.setField(assignment, "id", UUID.randomUUID());
            return assignment;
        });

        service.ensureAdmin(userId);

        verify(repository).save(any(PlatformRoleAssignment.class));
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(captor.capture());
        PlatformRoleAssignedEvent event = (PlatformRoleAssignedEvent) captor.getValue();
        assertThat(event.userId()).isEqualTo(userId);
        assertThat(event.role()).isEqualTo(PlatformRole.PLATFORM_ADMIN.name());
    }

    @Test
    void shouldKeepExistingAdminAssignment() {
        UUID userId = UUID.randomUUID();
        when(repository.findByUserId(userId)).thenReturn(Optional.of(assignment(userId, 1L)));

        service.ensureAdmin(userId);

        verify(repository, never()).save(any(PlatformRoleAssignment.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void shouldPublishActivatedEventWhenReactivatingInactiveAdminAssignment() {
        UUID userId = UUID.randomUUID();
        PlatformRoleAssignment assignment = assignment(userId, 1L);
        assignment.deactivate();
        when(repository.findByUserId(userId)).thenReturn(Optional.of(assignment));

        service.ensureAdmin(userId);

        assertThat(assignment.getStatus()).isEqualTo(AuthorizationStatus.ACTIVE);
        verify(repository, never()).save(any(PlatformRoleAssignment.class));
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(captor.capture());
        PlatformRoleActivatedEvent event = (PlatformRoleActivatedEvent) captor.getValue();
        assertThat(event.assignmentId()).isEqualTo(assignment.getId());
        assertThat(event.userId()).isEqualTo(userId);
        assertThat(event.role()).isEqualTo(PlatformRole.PLATFORM_ADMIN.name());
    }

    private static PlatformRoleAssignment assignment(UUID userId, long authorizationVersion) {
        PlatformRoleAssignment assignment = PlatformRoleAssignment.create(userId, PlatformRole.PLATFORM_ADMIN);
        ReflectionTestUtils.setField(assignment, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(assignment, "authorizationVersion", authorizationVersion);
        return assignment;
    }
}
