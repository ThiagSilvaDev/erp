package com.thiagsilvadev.erpapi.user.internal.controller;

import java.net.URI;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.status(HttpStatus.CREATED).location(location).body(response);
    }

    @Override
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(getUserByIdUseCase.execute(id));
    }

    @Override
    public ResponseEntity<Page<UserResponse>> listUsers(UserSearchCriteria criteria, Pageable pageable) {
        return ResponseEntity.ok(listUsersUseCase.execute(criteria, pageable));
    }
}
