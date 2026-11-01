package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ConflictException;
import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.security.AuditActorResolver;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.AuthorizationStatus;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.TenantMembershipActivatedEvent;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.TenantMembershipDeactivatedEvent;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.TenantMembershipProvisionedEvent;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.TenantMembershipRolesReplacedEvent;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.AuthorizationScopeType;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.TenantMembership;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.TenantRole;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.TenantRoleAssignment;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.TenantMembershipRepository;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.TenantRoleAssignmentRepository;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.TenantRoleRepository;
import com.thiagsilvadev.erpapi.user.api.UserFacade;

@Service
public class TenantMembershipService {

    private final TenantMembershipRepository membershipRepository;
    private final TenantRoleAssignmentRepository assignmentRepository;
    private final TenantRoleRepository roleRepository;
    private final RoleTemplateService templateService;
    private final UserFacade userFacade;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditActorResolver actorResolver;

    public TenantMembershipService(
            TenantMembershipRepository membershipRepository,
            TenantRoleAssignmentRepository assignmentRepository,
            TenantRoleRepository roleRepository,
            RoleTemplateService templateService,
            UserFacade userFacade,
            ApplicationEventPublisher eventPublisher,
            AuditActorResolver actorResolver) {
        this.membershipRepository = membershipRepository;
        this.assignmentRepository = assignmentRepository;
        this.roleRepository = roleRepository;
        this.templateService = templateService;
        this.userFacade = userFacade;
        this.eventPublisher = eventPublisher;
        this.actorResolver = actorResolver;
    }

    @Transactional
    public void provision(UUID userId, UUID companyId, Set<UUID> roleIds) {
        provisionMembership(userId, companyId, roleIds);
    }

    private void provisionMembership(UUID userId, UUID companyId, Set<UUID> roleIds) {
        userFacade.requireUser(userId);
        Optional<TenantMembership> existing = membershipRepository.findByUserIdAndCompanyId(userId, companyId);
        TenantMembership membership =
                existing.orElseGet(() -> membershipRepository.saveAndFlush(TenantMembership.create(userId, companyId)));
        boolean created = existing.isEmpty();
        if (membership.getStatus() == AuthorizationStatus.INACTIVE) {
            membership.activate();
            eventPublisher.publishEvent(new TenantMembershipActivatedEvent(
                    companyId,
                    membership.getId(),
                    membership.getUserId(),
                    tenantAssignmentRoleIds(membership),
                    actorResolver.userIdOrNull()));
        }
        replaceRoles(membership, roleIds, false);
        if (created) {
            eventPublisher.publishEvent(new TenantMembershipProvisionedEvent(
                    companyId, membership.getId(), userId, roleIds, actorResolver.userIdOrNull()));
        }
    }

    @Transactional
    public void provisionBuiltIn(UUID userId, UUID companyId, String roleCode) {
        templateService.ensureDefaults(companyId);
        TenantRole role = roleRepository
                .findByCompanyIdAndCode(companyId, roleCode)
                .orElseThrow(() -> new ResourceNotFoundException(TenantRole.class, roleCode));
        provisionMembership(userId, companyId, Set.of(role.getId()));
    }

    @Transactional
    public void replaceRoles(UUID membershipId, UUID companyId, Set<UUID> roleIds) {
        TenantMembership membership = membershipRepository
                .findByIdAndCompanyId(membershipId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException(TenantMembership.class, membershipId));
        replaceRoles(membership, roleIds, true);
    }

    @Transactional
    public void activate(UUID membershipId, UUID companyId) {
        TenantMembership membership = requireMembership(membershipId, companyId);
        List<TenantRoleAssignment> assignments = tenantAssignments(membership);
        if (assignments.isEmpty()) {
            throw new ConflictException("A membership must have at least one role before it can be activated.");
        }
        if (membership.getStatus() == AuthorizationStatus.INACTIVE) {
            membership.activate();
            eventPublisher.publishEvent(new TenantMembershipActivatedEvent(
                    companyId,
                    membershipId,
                    membership.getUserId(),
                    roleIdsOf(assignments),
                    actorResolver.userIdOrNull()));
        }
    }

    @Transactional
    public void deactivate(UUID membershipId, UUID companyId) {
        TenantMembership membership = requireMembership(membershipId, companyId);
        if (membership.getStatus() == AuthorizationStatus.INACTIVE) {
            return;
        }
        Set<UUID> roleIds = roleIdsOf(tenantAssignments(membership));
        if (removesLastOwner(membership, roleIds, Set.of())) {
            throw new ConflictException("A tenant must retain at least one active owner.");
        }
        membership.deactivate();
        eventPublisher.publishEvent(new TenantMembershipDeactivatedEvent(
                companyId, membershipId, membership.getUserId(), roleIds, actorResolver.userIdOrNull()));
    }

    private TenantMembership requireMembership(UUID membershipId, UUID companyId) {
        return membershipRepository
                .findByIdAndCompanyId(membershipId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException(TenantMembership.class, membershipId));
    }

    private void replaceRoles(TenantMembership membership, Set<UUID> roleIds, boolean enforceLastOwner) {
        if (roleIds == null || roleIds.isEmpty()) {
            throw new IllegalArgumentException("At least one role is required.");
        }
        Set<TenantRole> roles = new HashSet<>(roleRepository.findAllById(roleIds));
        if (roles.size() != roleIds.size()
                || roles.stream()
                        .anyMatch(role -> !role.getCompanyId().equals(membership.getCompanyId())
                                || role.getStatus() != AuthorizationStatus.ACTIVE)) {
            throw new AccessDeniedException("Every assigned role must be active and belong to the current tenant.");
        }

        Set<UUID> existingRoleIds = tenantAssignments(membership).stream()
                .map(TenantRoleAssignment::getRoleId)
                .collect(Collectors.toSet());
        if (existingRoleIds.equals(roleIds)) {
            return;
        }
        if (enforceLastOwner
                && membership.getStatus() == AuthorizationStatus.ACTIVE
                && removesLastOwner(membership, existingRoleIds, roleIds)) {
            throw new ConflictException("A tenant must retain at least one active owner.");
        }

        boolean hadAssignments = !existingRoleIds.isEmpty();
        Set<UUID> removedRoleIds = new HashSet<>(existingRoleIds);
        removedRoleIds.removeAll(roleIds);
        Set<UUID> addedRoleIds = new HashSet<>(roleIds);
        addedRoleIds.removeAll(existingRoleIds);
        if (!removedRoleIds.isEmpty()) {
            assignmentRepository.deleteByMembershipIdAndScopeTypeAndScopeIdAndRoleIdIn(
                    membership.getId(), AuthorizationScopeType.TENANT, membership.getCompanyId(), removedRoleIds);
        }
        if (!addedRoleIds.isEmpty()) {
            assignmentRepository.saveAll(addedRoleIds.stream()
                    .map(roleId -> TenantRoleAssignment.tenant(membership.getId(), roleId, membership.getCompanyId()))
                    .toList());
        }
        membership.touchAuthorization();
        if (hadAssignments) {
            eventPublisher.publishEvent(new TenantMembershipRolesReplacedEvent(
                    membership.getCompanyId(),
                    membership.getId(),
                    existingRoleIds,
                    roleIds,
                    actorResolver.userIdOrNull()));
        }
    }

    private boolean removesLastOwner(TenantMembership membership, Set<UUID> before, Set<UUID> after) {
        return roleRepository
                .findByCompanyIdAndCodeForUpdate(membership.getCompanyId(), RoleTemplateService.OWNER)
                .filter(owner -> before.contains(owner.getId()) && !after.contains(owner.getId()))
                .filter(owner -> assignmentRepository.countActiveAssignments(
                                owner.getId(),
                                membership.getCompanyId(),
                                AuthorizationStatus.ACTIVE,
                                AuthorizationScopeType.TENANT)
                        <= 1)
                .isPresent();
    }

    private List<TenantRoleAssignment> tenantAssignments(TenantMembership membership) {
        return assignmentRepository.findByMembershipIdAndScopeTypeAndScopeId(
                membership.getId(), AuthorizationScopeType.TENANT, membership.getCompanyId());
    }

    private Set<UUID> tenantAssignmentRoleIds(TenantMembership membership) {
        return roleIdsOf(tenantAssignments(membership));
    }

    private static Set<UUID> roleIdsOf(List<TenantRoleAssignment> assignments) {
        return assignments.stream().map(TenantRoleAssignment::getRoleId).collect(Collectors.toSet());
    }
}
