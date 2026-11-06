package com.thiagsilvadev.erpapi.user.internal.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.thiagsilvadev.erpapi.user.api.UserSnapshot;
import com.thiagsilvadev.erpapi.user.api.UserStatus;
import com.thiagsilvadev.erpapi.user.internal.dto.CreateUserRequest;
import com.thiagsilvadev.erpapi.user.internal.dto.UserSearchCriteria;
import com.thiagsilvadev.erpapi.user.internal.usecase.CreateUserUseCase;
import com.thiagsilvadev.erpapi.user.internal.usecase.UserQueryUseCase;

class UserControllerTest {

    private CreateUserUseCase createUserUseCase;
    private UserQueryUseCase userQueryUseCase;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        createUserUseCase = mock(CreateUserUseCase.class);
        userQueryUseCase = mock(UserQueryUseCase.class);
        mockMvc = standaloneSetup(new UserController(createUserUseCase, userQueryUseCase))
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void shouldCreateUser() throws Exception {
        UUID id = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        when(createUserUseCase.execute(any(CreateUserRequest.class))).thenReturn(userSnapshot(id));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {
                                  "firstName": "User",
                                  "lastName": "Example",
                                  "email": "user@example.com",
                                  "phone": "11912345678",
                                  "birthDate": "1990-01-01",
                                  "roleIds": ["%s"],
                                  "password": "secret123"
                                }
                                """
                                        .formatted(roleId)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/users/" + id))
                .andExpect(jsonPath("$.displayName").value("User Example"))
                .andExpect(jsonPath("$.email").value("user@example.com"));
    }

    @Test
    void shouldGetUserById() throws Exception {
        UUID id = UUID.randomUUID();
        when(userQueryUseCase.getById(id)).thenReturn(userSnapshot(id));

        mockMvc.perform(get("/api/v1/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void shouldListUsers() throws Exception {
        UUID id = UUID.randomUUID();
        when(userQueryUseCase.list(any(UserSearchCriteria.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(userSnapshot(id)), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].displayName").value("User Example"));
    }

    private static UserSnapshot userSnapshot(UUID id) {
        return new UserSnapshot(
                id,
                "User Example",
                "User",
                "Example",
                "user@example.com",
                "11912345678",
                LocalDate.of(1990, 1, 1),
                UserStatus.ACTIVE);
    }
}
