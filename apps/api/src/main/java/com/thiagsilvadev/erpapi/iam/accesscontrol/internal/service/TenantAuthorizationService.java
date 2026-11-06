package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.service;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.AuthorizationStatus;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.TenantAuthorizationSnapshot;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.AuthorizationScopeType;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.TenantMembership;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.TenantRole;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.RolePermissionRepository;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.TenantMembershipRepository;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.TenantRoleAssignmentRepository;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.TenantRoleRepository;

@Service
public class TenantAuthorizationService {

    private final TenantMembershipRepository membershipRepository;
    private final TenantRoleAssignmentRepository assignmentRepository;
    private final TenantRoleRepository roleRepository;
    private final RolePermissionRepository permissionRepository;

    public TenantAuthorizationService(
            TenantMembershipRepository membershipRepository,
            TenantRoleAssignmentRepository assignmentRepository,
            TenantRoleRepository roleRepository,
            RolePermissionRepository permissionRepository) {
        this.membershipRepository = membershipRepository;
        this.assignmentRepository = assignmentRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Transactional(readOnly = true)
    public List<TenantAuthorizationSnapshot> getActiveMemberships(UUID userId) {
        return membershipRepository.findByUserIdAndStatus(userId, AuthorizationStatus.ACTIVE).stream()
                .map(this::snapshot)
                .toList();
    }

    @Transactional(readOnly = true)
    public TenantAuthorizationSnapshot getActiveMembership(UUID userId, UUID companyId) {
        TenantMembership membership = membershipRepository
                .findByUserIdAndCompanyId(userId, companyId)
                .filter(item -> item.getStatus() == AuthorizationStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("TenantMembership", userId + "/" + companyId));
        return snapshot(membership);
    }

    @Transactional(readOnly = true)
    public TenantAuthorizationSnapshot validateScopedToken(
            UUID membershipId, UUID userId, UUID companyId, long authorizationVersion) {
        TenantMembership membership = membershipRepository
                .findByIdAndUserIdAndCompanyId(membershipId, userId, companyId)
                .filter(item -> item.getStatus() == AuthorizationStatus.ACTIVE)
                .orElseThrow(() -> new AccessDeniedException("Tenant membership is no longer active."));
        if (membership.getAuthorizationVersion() != authorizationVersion) {
            throw new AccessDeniedException("Tenant authorization has changed. Select the company again.");
        }
        return snapshot(membership);
    }

    @Transactional(readOnly = true)
    public TenantAuthorizationSnapshot getMembership(UUID membershipId, UUID companyId) {
        TenantMembership membership = membershipRepository
                .findByIdAndCompanyId(membershipId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException(TenantMembership.class, membershipId));
        return snapshot(membership);
    }

    @Transactional(readOnly = true)
    public List<TenantAuthorizationSnapshot> getMemberships(UUID companyId) {
        return membershipRepository.findByCompanyIdOrderByCreatedAt(companyId).stream()
                .map(this::snapshot)
                .toList();
    }

    private TenantAuthorizationSnapshot snapshot(TenantMembership membership) {
        Set<UUID> roleIds = assignmentRepository
                .findByMembershipIdAndScopeTypeAndScopeId(
                        membership.getId(), AuthorizationScopeType.TENANT, membership.getCompanyId())
                .stream()
                .map(assignment -> assignment.getRoleId())
                .collect(Collectors.toSet());
        if (roleIds.isEmpty()) {
            return new TenantAuthorizationSnapshot(
                    membership.getId(),
                    membership.getUserId(),
                    membership.getCompanyId(),
                    membership.getStatus(),
                    membership.getAuthorizationVersion(),
                    List.of(),
                    Set.of());
        }

        var rolesById = roleRepository.findAllById(roleIds).stream()
                .filter(role -> role.getCompanyId().equals(membership.getCompanyId()))
                .filter(role -> role.getStatus() == AuthorizationStatus.ACTIVE)
                .collect(Collectors.toMap(TenantRole::getId, Function.identity()));
        Set<String> permissions = permissionRepository.findByRoleIdIn(rolesById.keySet()).stream()
                .map(permission -> permission.getPermissionKey())
                .collect(Collectors.toCollection(HashSet::new));
        var roles = rolesById.values().stream()
                .map(role -> new TenantAuthorizationSnapshot.RoleSummary(role.getId(), role.getCode(), role.getName()))
                .sorted(Comparator.comparing(TenantAuthorizationSnapshot.RoleSummary::name))
                .toList();

        return new TenantAuthorizationSnapshot(
                membership.getId(),
                membership.getUserId(),
                membership.getCompanyId(),
                membership.getStatus(),
                membership.getAuthorizationVersion(),
                roles,
                Set.copyOf(permissions));
    }
}
