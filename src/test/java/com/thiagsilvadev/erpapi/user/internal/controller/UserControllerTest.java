package com.thiagsilvadev.erpapi.user.internal.controller;

import static org.mockito.ArgumentMatchers.any;
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

import com.thiagsilvadev.erpapi.user.api.UserRole;
import com.thiagsilvadev.erpapi.user.api.UserStatus;
import com.thiagsilvadev.erpapi.user.internal.dto.CreateUserRequest;
import com.thiagsilvadev.erpapi.user.internal.dto.UserResponse;
import com.thiagsilvadev.erpapi.user.internal.dto.UserSearchCriteria;
import com.thiagsilvadev.erpapi.user.internal.usecase.CreateUserUseCase;
import com.thiagsilvadev.erpapi.user.internal.usecase.GetUserByIdUseCase;
import com.thiagsilvadev.erpapi.user.internal.usecase.ListUsersUseCase;

class UserControllerTest {

    private CreateUserUseCase createUserUseCase;
    private GetUserByIdUseCase getUserByIdUseCase;
    private ListUsersUseCase listUsersUseCase;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        createUserUseCase = org.mockito.Mockito.mock(CreateUserUseCase.class);
        getUserByIdUseCase = org.mockito.Mockito.mock(GetUserByIdUseCase.class);
        listUsersUseCase = org.mockito.Mockito.mock(ListUsersUseCase.class);
        mockMvc = standaloneSetup(new UserController(createUserUseCase, getUserByIdUseCase, listUsersUseCase))
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void shouldCreateUser() throws Exception {
        UUID id = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        when(createUserUseCase.execute(any(CreateUserRequest.class))).thenReturn(userResponse(id, companyId));

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
                                  "role": "ADMIN",
                                  "companyId": "%s",
                                  "password": "secret123"
                                }
                                """
                                        .formatted(companyId)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/users/" + id))
                .andExpect(jsonPath("$.displayName").value("User Example"))
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.companyId").value(companyId.toString()));
    }

    @Test
    void shouldGetUserById() throws Exception {
        UUID id = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        when(getUserByIdUseCase.execute(id)).thenReturn(userResponse(id, companyId));

        mockMvc.perform(get("/api/v1/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void shouldListUsers() throws Exception {
        UUID id = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        when(listUsersUseCase.execute(any(UserSearchCriteria.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(userResponse(id, companyId)), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].displayName").value("User Example"));
    }

    private static UserResponse userResponse(UUID id, UUID companyId) {
        return new UserResponse(
                id,
                "User Example",
                "User",
                "Example",
                "user@example.com",
                "11912345678",
                LocalDate.of(1990, 1, 1),
                UserRole.ADMIN,
                UserStatus.ACTIVE,
                companyId);
    }
}
