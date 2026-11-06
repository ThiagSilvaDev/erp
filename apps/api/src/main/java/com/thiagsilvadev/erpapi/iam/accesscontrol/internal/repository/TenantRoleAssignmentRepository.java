package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.thiagsilvadev.erpapi.iam.accesscontrol.api.AuthorizationStatus;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.AuthorizationScopeType;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.TenantRoleAssignment;

public interface TenantRoleAssignmentRepository extends JpaRepository<TenantRoleAssignment, UUID> {

    List<TenantRoleAssignment> findByMembershipId(UUID membershipId);

    List<TenantRoleAssignment> findByMembershipIdAndScopeTypeAndScopeId(
            UUID membershipId, AuthorizationScopeType scopeType, UUID scopeId);

    List<TenantRoleAssignment> findByMembershipIdIn(Collection<UUID> membershipIds);

    boolean existsByRoleId(UUID roleId);

    @Modifying
    @Query(
            """
            delete from TenantRoleAssignment a
            where a.membershipId = :membershipId
              and a.scopeType = :scopeType
              and a.scopeId = :scopeId
            """)
    void deleteByMembershipIdAndScopeTypeAndScopeId(
            @Param("membershipId") UUID membershipId,
            @Param("scopeType") AuthorizationScopeType scopeType,
            @Param("scopeId") UUID scopeId);

    @Modifying
    @Query(
            """
            delete from TenantRoleAssignment a
            where a.membershipId = :membershipId
              and a.scopeType = :scopeType
              and a.scopeId = :scopeId
              and a.roleId in :roleIds
            """)
    void deleteByMembershipIdAndScopeTypeAndScopeIdAndRoleIdIn(
            @Param("membershipId") UUID membershipId,
            @Param("scopeType") AuthorizationScopeType scopeType,
            @Param("scopeId") UUID scopeId,
            @Param("roleIds") Collection<UUID> roleIds);

    @Query("select distinct a.membershipId from TenantRoleAssignment a where a.roleId = :roleId")
    List<UUID> findMembershipIdsByRoleId(@Param("roleId") UUID roleId);

    @Query("select count(a) from TenantRoleAssignment a where a.roleId = :roleId")
    long countByRoleId(@Param("roleId") UUID roleId);

    @Query(
            """
            select count(a)
            from TenantRoleAssignment a
            join TenantMembership m on a.membershipId = m.id
            where a.roleId = :roleId
              and m.companyId = :companyId
              and m.status = :status
              and a.scopeType = :scopeType
              and a.scopeId = :companyId
            """)
    long countActiveAssignments(
            @Param("roleId") UUID roleId,
            @Param("companyId") UUID companyId,
            @Param("status") AuthorizationStatus status,
            @Param("scopeType") AuthorizationScopeType scopeType);
}
