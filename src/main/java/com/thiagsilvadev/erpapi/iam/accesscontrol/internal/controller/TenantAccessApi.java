package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.controller;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.dto.CreateTenantMembershipRequest;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.dto.CreateTenantRoleRequest;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.dto.ReplaceMembershipRolesRequest;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.dto.TenantMembershipResponse;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.dto.TenantRoleResponse;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.dto.UpdateTenantRoleRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping(value = "/api/v1/iam", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "iam")
public interface TenantAccessApi {

    @GetMapping("/roles")
    @Operation(operationId = "listTenantRoles")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "403")
    List<TenantRoleResponse> listRoles();

    @PostMapping(value = "/roles", consumes = "application/json")
    @Operation(operationId = "createTenantRole")
    @ApiResponse(responseCode = "201")
    @ApiResponse(responseCode = "400")
    @ApiResponse(responseCode = "403")
    ResponseEntity<TenantRoleResponse> createRole(@Valid @RequestBody CreateTenantRoleRequest request);

    @PutMapping(value = "/roles/{roleId}", consumes = "application/json")
    @Operation(operationId = "updateTenantRole")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "400")
    @ApiResponse(responseCode = "403")
    @ApiResponse(responseCode = "404")
    @ApiResponse(responseCode = "409")
    TenantRoleResponse updateRole(@PathVariable UUID roleId, @Valid @RequestBody UpdateTenantRoleRequest request);

    @DeleteMapping("/roles/{roleId}")
    @Operation(operationId = "deleteTenantRole")
    @ApiResponse(responseCode = "204")
    @ApiResponse(responseCode = "403")
    @ApiResponse(responseCode = "404")
    @ApiResponse(responseCode = "409")
    ResponseEntity<Void> deleteRole(@PathVariable UUID roleId);

    @PostMapping(value = "/memberships", consumes = "application/json")
    @Operation(operationId = "createTenantMembership")
    @ApiResponse(responseCode = "201")
    @ApiResponse(responseCode = "400")
    @ApiResponse(responseCode = "403")
    @ApiResponse(responseCode = "404")
    ResponseEntity<TenantMembershipResponse> createMembership(
            @Valid @RequestBody CreateTenantMembershipRequest request);

    @GetMapping("/memberships")
    @Operation(operationId = "listTenantMemberships")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "403")
    List<TenantMembershipResponse> listMemberships();

    @PutMapping(value = "/memberships/{membershipId}/roles", consumes = "application/json")
    @Operation(operationId = "replaceTenantMembershipRoles")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "400")
    @ApiResponse(responseCode = "403")
    @ApiResponse(responseCode = "404")
    @ApiResponse(responseCode = "409")
    TenantMembershipResponse replaceMembershipRoles(
            @PathVariable UUID membershipId, @Valid @RequestBody ReplaceMembershipRolesRequest request);

    @PostMapping("/memberships/{membershipId}/activate")
    @Operation(operationId = "activateTenantMembership")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "403")
    @ApiResponse(responseCode = "404")
    @ApiResponse(responseCode = "409")
    TenantMembershipResponse activateMembership(@PathVariable UUID membershipId);

    @PostMapping("/memberships/{membershipId}/deactivate")
    @Operation(operationId = "deactivateTenantMembership")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "403")
    @ApiResponse(responseCode = "404")
    @ApiResponse(responseCode = "409")
    TenantMembershipResponse deactivateMembership(@PathVariable UUID membershipId);
}
