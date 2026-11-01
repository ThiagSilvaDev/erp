package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.service;

import static com.thiagsilvadev.erpapi.iam.accesscontrol.api.AuthorizationStatus.ACTIVE;
import static com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.AuthorizationScopeType.TENANT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import com.thiagsilvadev.erpapi.common.domain.exception.ConflictException;
import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.security.AuditActorResolver;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.TenantMembershipActivatedEvent;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.TenantMembershipDeactivatedEvent;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.TenantMembershipProvisionedEvent;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.TenantMembershipRolesReplacedEvent;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.TenantMembership;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.TenantRole;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.TenantRoleAssignment;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.TenantMembershipRepository;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.TenantRoleAssignmentRepository;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.TenantRoleRepository;
import com.thiagsilvadev.erpapi.user.api.UserFacade;

class TenantMembershipServiceTest {

    private final TenantMembershipRepository membershipRepository = mock(TenantMembershipRepository.class);
    private final TenantRoleAssignmentRepository assignmentRepository = mock(TenantRoleAssignmentRepository.class);
    private final TenantRoleRepository roleRepository = mock(TenantRoleRepository.class);
    private final RoleTemplateService templateService = mock(RoleTemplateService.class);
    private final UserFacade userFacade = mock(UserFacade.class);
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
    private final TenantMembershipService service = new TenantMembershipService(
            membershipRepository,
            assignmentRepository,
            roleRepository,
            templateService,
            userFacade,
            eventPublisher,
            mock(AuditActorResolver.class));

    @Test
    void shouldRejectRoleFromAnotherTenant() {
        UUID companyId = UUID.randomUUID();
        TenantMembership membership = membership(companyId);
        TenantRole foreignRole = role(UUID.randomUUID(), "ADMIN");
        when(membershipRepository.findByIdAndCompanyId(membership.getId(), companyId))
                .thenReturn(Optional.of(membership));
        when(roleRepository.findAllById(Set.of(foreignRole.getId()))).thenReturn(List.of(foreignRole));

        assertThatThrownBy(() -> service.replaceRoles(membership.getId(), companyId, Set.of(foreignRole.getId())))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("current tenant");
    }

    @Test
    void shouldRejectProvisionForMissingUser() {
        UUID companyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("User", userId)).when(userFacade).requireUser(userId);

        assertThatThrownBy(() -> service.provision(userId, companyId, Set.of(UUID.randomUUID())))
                .isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(membershipRepository);
    }

    @Test
    void shouldProvisionMembershipWhenProvisionBuiltInResolvesRole() {
        UUID companyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        TenantRole role = role(companyId, RoleTemplateService.ADMIN);
        when(roleRepository.findByCompanyIdAndCode(companyId, RoleTemplateService.ADMIN))
                .thenReturn(Optional.of(role));
        when(membershipRepository.findByUserIdAndCompanyId(userId, companyId)).thenReturn(Optional.empty());
        when(membershipRepository.saveAndFlush(any(TenantMembership.class))).thenAnswer(invocation -> {
            TenantMembership membership = invocation.getArgument(0);
            ReflectionTestUtils.setField(membership, "id", UUID.randomUUID());
            return membership;
        });
        when(roleRepository.findAllById(Set.of(role.getId()))).thenReturn(List.of(role));
        when(assignmentRepository.findByMembershipIdAndScopeTypeAndScopeId(any(), any(), any()))
                .thenReturn(List.of());

        service.provisionBuiltIn(userId, companyId, RoleTemplateService.ADMIN);

        TenantMembershipProvisionedEvent event = publishedEvent(TenantMembershipProvisionedEvent.class);
        assertThat(event.companyId()).isEqualTo(companyId);
        assertThat(event.userId()).isEqualTo(userId);
        assertThat(event.roleIds()).containsExactly(role.getId());
    }

    @Test
    void shouldPreventRemovingTheLastActiveOwner() {
        UUID companyId = UUID.randomUUID();
        TenantMembership membership = membership(companyId);
        TenantRole owner = role(companyId, RoleTemplateService.OWNER);
        TenantRole admin = role(companyId, RoleTemplateService.ADMIN);
        when(membershipRepository.findByIdAndCompanyId(membership.getId(), companyId))
                .thenReturn(Optional.of(membership));
        when(roleRepository.findAllById(Set.of(admin.getId()))).thenReturn(List.of(admin));
        when(assignmentRepository.findByMembershipIdAndScopeTypeAndScopeId(membership.getId(), TENANT, companyId))
                .thenReturn(List.of(TenantRoleAssignment.tenant(membership.getId(), owner.getId(), companyId)));
        when(roleRepository.findByCompanyIdAndCodeForUpdate(companyId, RoleTemplateService.OWNER))
                .thenReturn(Optional.of(owner));
        when(assignmentRepository.countActiveAssignments(owner.getId(), companyId, ACTIVE, TENANT))
                .thenReturn(1L);

        assertThatThrownBy(() -> service.replaceRoles(membership.getId(), companyId, Set.of(admin.getId())))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("active owner");
    }

    @Test
    void shouldAllowRoleReplacementOnInactiveMembershipWhenAnotherActiveOwnerExists() {
        UUID companyId = UUID.randomUUID();
        TenantMembership membership = membership(companyId);
        membership.deactivate();
        TenantRole owner = role(companyId, RoleTemplateService.OWNER);
        TenantRole admin = role(companyId, RoleTemplateService.ADMIN);
        when(membershipRepository.findByIdAndCompanyId(membership.getId(), companyId))
                .thenReturn(Optional.of(membership));
        when(roleRepository.findAllById(Set.of(admin.getId()))).thenReturn(List.of(admin));
        when(assignmentRepository.findByMembershipIdAndScopeTypeAndScopeId(membership.getId(), TENANT, companyId))
                .thenReturn(List.of(TenantRoleAssignment.tenant(membership.getId(), owner.getId(), companyId)));
        when(roleRepository.findByCompanyIdAndCode(companyId, RoleTemplateService.OWNER))
                .thenReturn(Optional.of(owner));
        when(assignmentRepository.countActiveAssignments(owner.getId(), companyId, ACTIVE, TENANT))
                .thenReturn(1L);

        service.replaceRoles(membership.getId(), companyId, Set.of(admin.getId()));

        verify(assignmentRepository)
                .deleteByMembershipIdAndScopeTypeAndScopeIdAndRoleIdIn(
                        membership.getId(), TENANT, companyId, Set.of(owner.getId()));
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<TenantRoleAssignment>> savedCaptor = ArgumentCaptor.forClass(List.class);
        verify(assignmentRepository).saveAll(savedCaptor.capture());
        assertThat(savedCaptor.getValue())
                .extracting(TenantRoleAssignment::getRoleId)
                .containsExactly(admin.getId());
        TenantMembershipRolesReplacedEvent event = publishedEvent(TenantMembershipRolesReplacedEvent.class);
        assertThat(event.companyId()).isEqualTo(companyId);
        assertThat(event.membershipId()).isEqualTo(membership.getId());
        assertThat(event.beforeRoleIds()).containsExactly(owner.getId());
        assertThat(event.afterRoleIds()).containsExactly(admin.getId());
    }

    @Test
    void shouldOnlyTouchChangedAssignmentsWhenReplacingRoles() {
        UUID companyId = UUID.randomUUID();
        TenantMembership membership = membership(companyId);
        TenantRole owner = role(companyId, RoleTemplateService.OWNER);
        TenantRole admin = role(companyId, RoleTemplateService.ADMIN);
        TenantRole manager = role(companyId, "MANAGER");
        when(membershipRepository.findByIdAndCompanyId(membership.getId(), companyId))
                .thenReturn(Optional.of(membership));
        when(roleRepository.findAllById(Set.of(admin.getId(), manager.getId()))).thenReturn(List.of(admin, manager));
        when(assignmentRepository.findByMembershipIdAndScopeTypeAndScopeId(membership.getId(), TENANT, companyId))
                .thenReturn(List.of(
                        TenantRoleAssignment.tenant(membership.getId(), owner.getId(), companyId),
                        TenantRoleAssignment.tenant(membership.getId(), admin.getId(), companyId)));

        service.replaceRoles(membership.getId(), companyId, Set.of(admin.getId(), manager.getId()));

        verify(assignmentRepository)
                .deleteByMembershipIdAndScopeTypeAndScopeIdAndRoleIdIn(
                        membership.getId(), TENANT, companyId, Set.of(owner.getId()));
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<TenantRoleAssignment>> savedCaptor = ArgumentCaptor.forClass(List.class);
        verify(assignmentRepository).saveAll(savedCaptor.capture());
        assertThat(savedCaptor.getValue())
                .extracting(TenantRoleAssignment::getRoleId)
                .containsExactly(manager.getId());
        assertThat(membership.getAuthorizationVersion()).isEqualTo(1L);
    }

    @Test
    void shouldNotBumpAuthorizationVersionWhenReprovisioningWithSameRoles() {
        UUID companyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        TenantMembership membership = membership(companyId);
        ReflectionTestUtils.setField(membership, "userId", userId);
        ReflectionTestUtils.setField(membership, "authorizationVersion", 7L);
        TenantRole role = role(companyId, RoleTemplateService.ADMIN);
        when(membershipRepository.findByUserIdAndCompanyId(userId, companyId)).thenReturn(Optional.of(membership));
        when(roleRepository.findAllById(Set.of(role.getId()))).thenReturn(List.of(role));
        when(assignmentRepository.findByMembershipIdAndScopeTypeAndScopeId(membership.getId(), TENANT, companyId))
                .thenReturn(List.of(TenantRoleAssignment.tenant(membership.getId(), role.getId(), companyId)));

        service.provision(userId, companyId, Set.of(role.getId()));

        verify(assignmentRepository, never())
                .deleteByMembershipIdAndScopeTypeAndScopeIdAndRoleIdIn(any(), any(), any(), any());
        verify(assignmentRepository, never()).saveAll(any());
        assertThat(membership.getAuthorizationVersion()).isEqualTo(7L);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void shouldPublishProvisionedEventWhenMembershipIsCreated() {
        UUID companyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        TenantRole role = role(companyId, RoleTemplateService.ADMIN);
        when(membershipRepository.findByUserIdAndCompanyId(userId, companyId)).thenReturn(Optional.empty());
        when(membershipRepository.saveAndFlush(any(TenantMembership.class))).thenAnswer(invocation -> {
            TenantMembership membership = invocation.getArgument(0);
            ReflectionTestUtils.setField(membership, "id", UUID.randomUUID());
            return membership;
        });
        when(roleRepository.findAllById(Set.of(role.getId()))).thenReturn(List.of(role));
        when(assignmentRepository.findByMembershipIdAndScopeTypeAndScopeId(any(), any(), any()))
                .thenReturn(List.of());

        service.provision(userId, companyId, Set.of(role.getId()));

        TenantMembershipProvisionedEvent event = publishedEvent(TenantMembershipProvisionedEvent.class);
        assertThat(event.companyId()).isEqualTo(companyId);
        assertThat(event.userId()).isEqualTo(userId);
        assertThat(event.roleIds()).containsExactly(role.getId());
        verify(eventPublisher, never()).publishEvent(any(TenantMembershipRolesReplacedEvent.class));
    }

    @Test
    void shouldNotPublishProvisionedEventWhenReprovisioningExistingMembership() {
        UUID companyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        TenantMembership membership = membership(companyId);
        ReflectionTestUtils.setField(membership, "userId", userId);
        TenantRole owner = role(companyId, RoleTemplateService.OWNER);
        TenantRole admin = role(companyId, RoleTemplateService.ADMIN);
        when(membershipRepository.findByUserIdAndCompanyId(userId, companyId)).thenReturn(Optional.of(membership));
        when(roleRepository.findAllById(Set.of(admin.getId()))).thenReturn(List.of(admin));
        when(assignmentRepository.findByMembershipIdAndScopeTypeAndScopeId(membership.getId(), TENANT, companyId))
                .thenReturn(List.of(TenantRoleAssignment.tenant(membership.getId(), owner.getId(), companyId)));

        service.provision(userId, companyId, Set.of(admin.getId()));

        TenantMembershipRolesReplacedEvent replaced = publishedEvent(TenantMembershipRolesReplacedEvent.class);
        assertThat(replaced.afterRoleIds()).containsExactly(admin.getId());
        verify(eventPublisher, never()).publishEvent(any(TenantMembershipProvisionedEvent.class));
    }

    @Test
    void shouldPublishActivatedEventWhenProvisionReactivatesInactiveMembership() {
        UUID companyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        TenantMembership membership = membership(companyId);
        membership.deactivate();
        ReflectionTestUtils.setField(membership, "userId", userId);
        TenantRole role = role(companyId, RoleTemplateService.ADMIN);
        when(membershipRepository.findByUserIdAndCompanyId(userId, companyId)).thenReturn(Optional.of(membership));
        when(roleRepository.findAllById(Set.of(role.getId()))).thenReturn(List.of(role));
        when(assignmentRepository.findByMembershipIdAndScopeTypeAndScopeId(membership.getId(), TENANT, companyId))
                .thenReturn(List.of(TenantRoleAssignment.tenant(membership.getId(), role.getId(), companyId)));

        service.provision(userId, companyId, Set.of(role.getId()));

        TenantMembershipActivatedEvent event = publishedEvent(TenantMembershipActivatedEvent.class);
        assertThat(event.companyId()).isEqualTo(companyId);
        assertThat(event.membershipId()).isEqualTo(membership.getId());
        assertThat(event.userId()).isEqualTo(userId);
        assertThat(event.roleIds()).containsExactly(role.getId());
        assertThat(membership.getStatus()).isEqualTo(ACTIVE);
    }

    @Test
    void shouldPublishActivatedEventOnlyWhenMembershipIsInactive() {
        UUID companyId = UUID.randomUUID();
        TenantMembership membership = membership(companyId);
        when(membershipRepository.findByIdAndCompanyId(membership.getId(), companyId))
                .thenReturn(Optional.of(membership));
        when(assignmentRepository.findByMembershipIdAndScopeTypeAndScopeId(membership.getId(), TENANT, companyId))
                .thenReturn(List.of(TenantRoleAssignment.tenant(membership.getId(), UUID.randomUUID(), companyId)));

        service.activate(membership.getId(), companyId);
        verifyNoInteractions(eventPublisher);

        membership.deactivate();
        service.activate(membership.getId(), companyId);
        TenantMembershipActivatedEvent event = publishedEvent(TenantMembershipActivatedEvent.class);
        assertThat(event.membershipId()).isEqualTo(membership.getId());
    }

    @Test
    void shouldPublishDeactivatedEventOnlyWhenMembershipIsActive() {
        UUID companyId = UUID.randomUUID();
        TenantMembership membership = membership(companyId);
        membership.deactivate();
        when(membershipRepository.findByIdAndCompanyId(membership.getId(), companyId))
                .thenReturn(Optional.of(membership));

        service.deactivate(membership.getId(), companyId);
        verifyNoInteractions(eventPublisher);

        membership.activate();
        service.deactivate(membership.getId(), companyId);
        TenantMembershipDeactivatedEvent event = publishedEvent(TenantMembershipDeactivatedEvent.class);
        assertThat(event.membershipId()).isEqualTo(membership.getId());
    }

    private <T> T publishedEvent(Class<T> eventType) {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(captor.capture());
        return eventType.cast(captor.getValue());
    }

    private static TenantMembership membership(UUID companyId) {
        TenantMembership membership = TenantMembership.create(UUID.randomUUID(), companyId);
        ReflectionTestUtils.setField(membership, "id", UUID.randomUUID());
        return membership;
    }

    private static TenantRole role(UUID companyId, String code) {
        TenantRole role = TenantRole.builtIn(companyId, code, code, null);
        ReflectionTestUtils.setField(role, "id", UUID.randomUUID());
        return role;
    }
}
