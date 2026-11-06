package com.thiagsilvadev.erpapi.iam.internal.controller;

import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.BAD_REQUEST;
import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.FORBIDDEN;
import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.UNAUTHORIZED;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.thiagsilvadev.erpapi.iam.internal.dto.LoginRequest;
import com.thiagsilvadev.erpapi.iam.internal.dto.LoginResponse;
import com.thiagsilvadev.erpapi.iam.internal.dto.SelectCompanyRequest;
import com.thiagsilvadev.erpapi.iam.internal.dto.SelectCompanyResponse;
import com.thiagsilvadev.erpapi.iam.internal.dto.SelectPlatformResponse;
import com.thiagsilvadev.erpapi.iam.internal.dto.UserCompanyResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping(value = "/api/v1/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "authentication")
interface AuthApi {

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "authenticateUserAndIssueToken")
    @ApiResponse(responseCode = "200", description = "Authenticated and tokens issued")
    @ApiResponse(responseCode = "400", ref = BAD_REQUEST)
    @ApiResponse(responseCode = "401", ref = UNAUTHORIZED)
    LoginResponse login(@Valid @RequestBody LoginRequest request);

    @PostMapping(value = "/select-company", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            operationId = "selectActiveCompanyAndIssueScopedToken",
            description = "Requires a global bearer token (token_use=global).")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "Company selected and scoped token issued")
    @ApiResponse(responseCode = "400", ref = BAD_REQUEST)
    @ApiResponse(responseCode = "401", ref = UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = FORBIDDEN)
    SelectCompanyResponse selectCompany(
            @Valid @RequestBody SelectCompanyRequest request, @AuthenticationPrincipal Jwt jwt);

    @PostMapping("/select-platform")
    @Operation(operationId = "selectPlatform", description = "Requires a global bearer token (token_use=global).")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "Platform token issued")
    @ApiResponse(responseCode = "401", ref = UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = FORBIDDEN)
    SelectPlatformResponse selectPlatform(@AuthenticationPrincipal Jwt jwt);

    @GetMapping(value = "/companies")
    @Operation(operationId = "listUserCompanies", description = "Requires a global bearer token (token_use=global).")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "User companies retrieved")
    @ApiResponse(responseCode = "401", ref = UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = FORBIDDEN)
    List<UserCompanyResponse> listCompanies(@AuthenticationPrincipal Jwt jwt);
}
