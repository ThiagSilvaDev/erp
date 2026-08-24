package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.service;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.iam.accesscontrol.api.AuthorizationStatus;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.AuthorizationScopeType;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.TenantMembershipRepository;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.TenantRoleAssignmentRepository;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.TenantRoleRepository;
import com.thiagsilvadev.erpapi.user.api.TenantUserAccessFacade;

@Service
public class TenantUserAccessFacadeImpl implements TenantUserAccessFacade {

    private final TenantMembershipRepository membershipRepository;
    private final TenantRoleRepository roleRepository;
    private final TenantRoleAssignmentRepository assignmentRepository;

    public TenantUserAccessFacadeImpl(
            TenantMembershipRepository membershipRepository,
            TenantRoleRepository roleRepository,
            TenantRoleAssignmentRepository assignmentRepository) {
        this.membershipRepository = membershipRepository;
        this.roleRepository = roleRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Set<UUID> getActiveUserIds(UUID companyId) {
        return membershipRepository.findByCompanyIdAndStatus(companyId, AuthorizationStatus.ACTIVE).stream()
                .map(membership -> membership.getUserId())
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasActiveMembership(UUID userId, UUID companyId) {
        return membershipRepository
                .findByUserIdAndCompanyId(userId, companyId)
                .filter(membership -> membership.getStatus() == AuthorizationStatus.ACTIVE)
                .isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasRole(UUID userId, UUID companyId, String roleCode) {
        var membership = membershipRepository
                .findByUserIdAndCompanyId(userId, companyId)
                .filter(item -> item.getStatus() == AuthorizationStatus.ACTIVE);
        var role = roleRepository.findByCompanyIdAndCode(companyId, roleCode);
        if (membership.isEmpty() || role.isEmpty()) {
            return false;
        }
        return assignmentRepository
                .findByMembershipIdAndScopeTypeAndScopeId(
                        membership.get().getId(), AuthorizationScopeType.TENANT, companyId)
                .stream()
                .anyMatch(assignment -> assignment.getRoleId().equals(role.get().getId()));
    }
}
