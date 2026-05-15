package com.thiagsilvadev.erpapi.user.internal.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.thiagsilvadev.erpapi.user.internal.dto.CreateUserRequest;
import com.thiagsilvadev.erpapi.user.internal.dto.UserResponse;
import com.thiagsilvadev.erpapi.user.internal.dto.UserSearchCriteria;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping(value = "/api/v1/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "users", description = "User management endpoints")
public interface UserApi {

    @PostMapping
    @Operation(operationId = "createUser", summary = "Create a new user")
    @ApiResponse(responseCode = "201", description = "User created successfully")
    @ApiResponse(responseCode = "400", description = "Bad Request")
    @ApiResponse(responseCode = "422", description = "Unprocessable Entity")
    ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request);

    @GetMapping("/{id}")
    @Operation(operationId = "getUserById", summary = "Get a user by ID")
    @ApiResponse(responseCode = "200", description = "User retrieved successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    ResponseEntity<UserResponse> getUserById(@PathVariable UUID id);

    @GetMapping
    @Operation(operationId = "listUsers", summary = "List users")
    @ApiResponse(responseCode = "200", description = "Users listed successfully")
    ResponseEntity<Page<UserResponse>> listUsers(UserSearchCriteria criteria, Pageable pageable);
}
