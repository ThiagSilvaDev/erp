package com.thiagsilvadev.erpapi.user.internal.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.thiagsilvadev.erpapi.common.web.ResponseEntities;
import com.thiagsilvadev.erpapi.user.internal.dto.CreateUserRequest;
import com.thiagsilvadev.erpapi.user.internal.dto.UserResponse;
import com.thiagsilvadev.erpapi.user.internal.dto.UserSearchCriteria;
import com.thiagsilvadev.erpapi.user.internal.usecase.CreateUserUseCase;
import com.thiagsilvadev.erpapi.user.internal.usecase.GetUserByIdUseCase;
import com.thiagsilvadev.erpapi.user.internal.usecase.ListUsersUseCase;

@RestController
@Validated
class UserController implements UserApi {

    private final CreateUserUseCase createUserUseCase;
    private final GetUserByIdUseCase getUserByIdUseCase;
    private final ListUsersUseCase listUsersUseCase;

    UserController(
            CreateUserUseCase createUserUseCase,
            GetUserByIdUseCase getUserByIdUseCase,
            ListUsersUseCase listUsersUseCase) {
        this.createUserUseCase = createUserUseCase;
        this.getUserByIdUseCase = getUserByIdUseCase;
        this.listUsersUseCase = listUsersUseCase;
    }

    @Override
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = createUserUseCase.execute(request);
        return ResponseEntities.created(response.id(), response);
    }

    @Override
    public UserResponse getUserById(@PathVariable UUID id) {
        return getUserByIdUseCase.execute(id);
    }

    @Override
    public Page<UserResponse> listUsers(UserSearchCriteria criteria, Pageable pageable) {
        return listUsersUseCase.execute(criteria, pageable);
    }
}
