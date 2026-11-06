package com.thiagsilvadev.erpapi.user.internal.controller;

import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.BAD_REQUEST;
import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.CONFLICT;
import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.FORBIDDEN;
import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.NOT_FOUND;
import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.UNAUTHORIZED;
import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.UNPROCESSABLE_ENTITY;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.thiagsilvadev.erpapi.user.api.UserSnapshot;
import com.thiagsilvadev.erpapi.user.internal.dto.CreateUserRequest;
import com.thiagsilvadev.erpapi.user.internal.dto.UserSearchCriteria;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping(value = "/api/v1/users", produces = MediaType.APPLICATION_JSON_VALUE)
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "users")
interface UserApi {

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "createUser")
    @ApiResponse(responseCode = "201", description = "User created")
    @ApiResponse(responseCode = "400", ref = BAD_REQUEST)
    @ApiResponse(responseCode = "401", ref = UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = FORBIDDEN)
    @ApiResponse(responseCode = "409", ref = CONFLICT)
    @ApiResponse(responseCode = "422", ref = UNPROCESSABLE_ENTITY)
    ResponseEntity<UserSnapshot> create(@Valid @RequestBody CreateUserRequest request);

    @GetMapping("/{id}")
    @Operation(operationId = "getUserById")
    @ApiResponse(responseCode = "200", description = "User retrieved")
    @ApiResponse(responseCode = "400", ref = BAD_REQUEST)
    @ApiResponse(responseCode = "401", ref = UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = FORBIDDEN)
    @ApiResponse(responseCode = "404", ref = NOT_FOUND)
    UserSnapshot getById(@PathVariable UUID id);

    @GetMapping
    @Operation(operationId = "listUsers")
    @ApiResponse(responseCode = "200", description = "Users retrieved")
    @ApiResponse(responseCode = "400", ref = BAD_REQUEST)
    @ApiResponse(responseCode = "401", ref = UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = FORBIDDEN)
    Page<UserSnapshot> list(@Valid @ParameterObject UserSearchCriteria criteria, @ParameterObject Pageable pageable);
}
