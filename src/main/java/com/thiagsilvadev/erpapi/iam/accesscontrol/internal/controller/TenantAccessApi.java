package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.controller;

import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.BAD_REQUEST;
import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.CONFLICT;
import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.FORBIDDEN;
import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.NOT_FOUND;
import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.UNAUTHORIZED;

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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping(value = "/api/v1/iam", produces = MediaType.APPLICATION_JSON_VALUE)
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "iam")
interface TenantAccessApi {

    @GetMapping("/roles")
    @Operation(operationId = "listTenantRoles")
    @ApiResponse(responseCode = "200", description = "Tenant roles retrieved")
    @ApiResponse(responseCode = "401", ref = UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = FORBIDDEN)
    List<TenantRoleResponse> listRoles();

    @PostMapping(value = "/roles", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "createTenantRole")
    @ApiResponse(responseCode = "201", description = "Tenant role created")
    @ApiResponse(responseCode = "400", ref = BAD_REQUEST)
    @ApiResponse(responseCode = "401", ref = UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = FORBIDDEN)
    ResponseEntity<TenantRoleResponse> createRole(@Valid @RequestBody CreateTenantRoleRequest request);

    @PutMapping(value = "/roles/{roleId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "updateTenantRole")
    @ApiResponse(responseCode = "200", description = "Tenant role updated")
    @ApiResponse(responseCode = "400", ref = BAD_REQUEST)
    @ApiResponse(responseCode = "401", ref = UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = FORBIDDEN)
    @ApiResponse(responseCode = "404", ref = NOT_FOUND)
    @ApiResponse(responseCode = "409", ref = CONFLICT)
    TenantRoleResponse updateRole(@PathVariable UUID roleId, @Valid @RequestBody UpdateTenantRoleRequest request);

    @DeleteMapping("/roles/{roleId}")
    @Operation(operationId = "deleteTenantRole")
    @ApiResponse(responseCode = "204", description = "Tenant role deleted", content = @Content)
    @ApiResponse(responseCode = "400", ref = BAD_REQUEST)
    @ApiResponse(responseCode = "401", ref = UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = FORBIDDEN)
    @ApiResponse(responseCode = "404", ref = NOT_FOUND)
    @ApiResponse(responseCode = "409", ref = CONFLICT)
    ResponseEntity<Void> deleteRole(@PathVariable UUID roleId);

    @PostMapping(value = "/memberships", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "createTenantMembership")
    @ApiResponse(responseCode = "201", description = "Tenant membership created")
    @ApiResponse(responseCode = "400", ref = BAD_REQUEST)
    @ApiResponse(responseCode = "401", ref = UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = FORBIDDEN)
    @ApiResponse(responseCode = "404", ref = NOT_FOUND)
    ResponseEntity<TenantMembershipResponse> createMembership(
            @Valid @RequestBody CreateTenantMembershipRequest request);

    @GetMapping("/memberships")
    @Operation(operationId = "listTenantMemberships")
    @ApiResponse(responseCode = "200", description = "Tenant memberships retrieved")
    @ApiResponse(responseCode = "401", ref = UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = FORBIDDEN)
    List<TenantMembershipResponse> listMemberships();

    @PutMapping(value = "/memberships/{membershipId}/roles", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "replaceTenantMembershipRoles")
    @ApiResponse(responseCode = "200", description = "Membership roles replaced")
    @ApiResponse(responseCode = "400", ref = BAD_REQUEST)
    @ApiResponse(responseCode = "401", ref = UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = FORBIDDEN)
    @ApiResponse(responseCode = "404", ref = NOT_FOUND)
    @ApiResponse(responseCode = "409", ref = CONFLICT)
    TenantMembershipResponse replaceMembershipRoles(
            @PathVariable UUID membershipId, @Valid @RequestBody ReplaceMembershipRolesRequest request);

    @PostMapping("/memberships/{membershipId}/activate")
    @Operation(operationId = "activateTenantMembership")
    @ApiResponse(responseCode = "200", description = "Tenant membership activated")
    @ApiResponse(responseCode = "400", ref = BAD_REQUEST)
    @ApiResponse(responseCode = "401", ref = UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = FORBIDDEN)
    @ApiResponse(responseCode = "404", ref = NOT_FOUND)
    @ApiResponse(responseCode = "409", ref = CONFLICT)
    TenantMembershipResponse activateMembership(@PathVariable UUID membershipId);

    @PostMapping("/memberships/{membershipId}/deactivate")
    @Operation(operationId = "deactivateTenantMembership")
    @ApiResponse(responseCode = "200", description = "Tenant membership deactivated")
    @ApiResponse(responseCode = "400", ref = BAD_REQUEST)
    @ApiResponse(responseCode = "401", ref = UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = FORBIDDEN)
    @ApiResponse(responseCode = "404", ref = NOT_FOUND)
    @ApiResponse(responseCode = "409", ref = CONFLICT)
    TenantMembershipResponse deactivateMembership(@PathVariable UUID membershipId);
}
