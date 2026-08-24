package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.dto.CreateTenantMembershipRequest;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.dto.CreateTenantRoleRequest;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.dto.ReplaceMembershipRolesRequest;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.dto.TenantMembershipResponse;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.dto.TenantRoleResponse;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.dto.UpdateTenantRoleRequest;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.usecase.CreateTenantRoleUseCase;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.usecase.DeleteTenantRoleUseCase;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.usecase.ListTenantRolesUseCase;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.usecase.TenantMembershipUseCase;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.usecase.UpdateTenantRoleUseCase;

@RestController
class TenantAccessController implements TenantAccessApi {

    private final ListTenantRolesUseCase listRoles;
    private final CreateTenantRoleUseCase createRole;
    private final UpdateTenantRoleUseCase updateRole;
    private final DeleteTenantRoleUseCase deleteRole;
    private final TenantMembershipUseCase membershipUseCase;

    TenantAccessController(
            ListTenantRolesUseCase listRoles,
            CreateTenantRoleUseCase createRole,
            UpdateTenantRoleUseCase updateRole,
            DeleteTenantRoleUseCase deleteRole,
            TenantMembershipUseCase membershipUseCase) {
        this.listRoles = listRoles;
        this.createRole = createRole;
        this.updateRole = updateRole;
        this.deleteRole = deleteRole;
        this.membershipUseCase = membershipUseCase;
    }

    @Override
    public List<TenantRoleResponse> listRoles() {
        return listRoles.execute();
    }

    @Override
    public ResponseEntity<TenantRoleResponse> createRole(@Valid @RequestBody CreateTenantRoleRequest request) {
        TenantRoleResponse response = createRole.execute(request);
        return ResponseEntity.created(URI.create("/api/v1/iam/roles/" + response.id()))
                .body(response);
    }

    @Override
    public TenantRoleResponse updateRole(UUID roleId, @Valid @RequestBody UpdateTenantRoleRequest request) {
        return updateRole.execute(roleId, request);
    }

    @Override
    public ResponseEntity<Void> deleteRole(UUID roleId) {
        deleteRole.execute(roleId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<TenantMembershipResponse> createMembership(
            @Valid @RequestBody CreateTenantMembershipRequest request) {
        TenantMembershipResponse response = membershipUseCase.createMembership(request);
        return ResponseEntity.created(URI.create("/api/v1/iam/memberships/" + response.id()))
                .body(response);
    }

    @Override
    public List<TenantMembershipResponse> listMemberships() {
        return membershipUseCase.listMemberships();
    }

    @Override
    public TenantMembershipResponse replaceMembershipRoles(
            UUID membershipId, @Valid @RequestBody ReplaceMembershipRolesRequest request) {
        return membershipUseCase.replaceMembershipRoles(membershipId, request);
    }

    @Override
    public TenantMembershipResponse activateMembership(UUID membershipId) {
        return membershipUseCase.activateMembership(membershipId);
    }

    @Override
    public TenantMembershipResponse deactivateMembership(UUID membershipId) {
        return membershipUseCase.deactivateMembership(membershipId);
    }
}
