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
import com.thiagsilvadev.erpapi.user.api.UserSnapshot;
import com.thiagsilvadev.erpapi.user.internal.dto.CreateUserRequest;
import com.thiagsilvadev.erpapi.user.internal.dto.UserSearchCriteria;
import com.thiagsilvadev.erpapi.user.internal.usecase.CreateUserUseCase;
import com.thiagsilvadev.erpapi.user.internal.usecase.UserQueryUseCase;

@RestController
@Validated
class UserController implements UserApi {

    private final CreateUserUseCase createUserUseCase;
    private final UserQueryUseCase userQueryUseCase;

    UserController(CreateUserUseCase createUserUseCase, UserQueryUseCase userQueryUseCase) {
        this.createUserUseCase = createUserUseCase;
        this.userQueryUseCase = userQueryUseCase;
    }

    @Override
    public ResponseEntity<UserSnapshot> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserSnapshot user = createUserUseCase.execute(request);
        return ResponseEntities.created(user.id(), user);
    }

    @Override
    public UserSnapshot getUserById(@PathVariable UUID id) {
        return userQueryUseCase.getById(id);
    }

    @Override
    public Page<UserSnapshot> listUsers(UserSearchCriteria criteria, Pageable pageable) {
        return userQueryUseCase.list(criteria, pageable);
    }
}
