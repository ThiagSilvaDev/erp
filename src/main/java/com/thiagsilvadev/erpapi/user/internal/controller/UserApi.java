package com.thiagsilvadev.erpapi.user.internal.controller;

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

import com.thiagsilvadev.erpapi.user.internal.dto.CreateUserRequest;
import com.thiagsilvadev.erpapi.user.internal.dto.UserResponse;
import com.thiagsilvadev.erpapi.user.internal.dto.UserSearchCriteria;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping(value = "/api/v1/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "users")
public interface UserApi {

    @PostMapping(consumes = "application/json")
    @Operation(operationId = "createUser")
    @ApiResponse(responseCode = "201")
    @ApiResponse(responseCode = "400")
    @ApiResponse(responseCode = "422")
    ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request);

    @GetMapping("/{id}")
    @Operation(operationId = "getUserById")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "404")
    UserResponse getUserById(@PathVariable UUID id);

    @GetMapping
    @Operation(operationId = "listUsers")
    @ApiResponse(responseCode = "200")
    Page<UserResponse> listUsers(@ParameterObject UserSearchCriteria criteria, @ParameterObject Pageable pageable);
}
