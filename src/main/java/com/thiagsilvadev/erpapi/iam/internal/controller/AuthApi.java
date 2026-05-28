package com.thiagsilvadev.erpapi.iam.internal.controller;

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
import com.thiagsilvadev.erpapi.iam.internal.dto.UserCompanyResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping(value = "/api/v1/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "authentication")
public interface AuthApi {

    @PostMapping(value = "/login", consumes = "application/json")
    @Operation(operationId = "authenticateUserAndIssueToken")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401")
    @ApiResponse(responseCode = "400")
    LoginResponse login(@Valid @RequestBody LoginRequest request);

    @PostMapping(value = "/select-company", consumes = "application/json")
    @Operation(operationId = "selectActiveCompanyAndIssueScopedToken")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401")
    @ApiResponse(responseCode = "403")
    @ApiResponse(responseCode = "400")
    SelectCompanyResponse selectCompany(
            @Valid @RequestBody SelectCompanyRequest request, @AuthenticationPrincipal Jwt jwt);

    @GetMapping(value = "/companies")
    @Operation(operationId = "listUserCompanies")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401")
    List<UserCompanyResponse> listCompanies(@AuthenticationPrincipal Jwt jwt);
}
