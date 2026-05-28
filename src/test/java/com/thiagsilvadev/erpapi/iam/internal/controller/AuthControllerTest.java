package com.thiagsilvadev.erpapi.iam.internal.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.thiagsilvadev.erpapi.iam.internal.dto.AuthUserResponse;
import com.thiagsilvadev.erpapi.iam.internal.dto.LoginRequest;
import com.thiagsilvadev.erpapi.iam.internal.dto.LoginResponse;
import com.thiagsilvadev.erpapi.iam.internal.usecase.ListUserCompaniesUseCase;
import com.thiagsilvadev.erpapi.iam.internal.usecase.LoginUseCase;
import com.thiagsilvadev.erpapi.iam.internal.usecase.SelectCompanyUseCase;

class AuthControllerTest {

    private LoginUseCase loginUseCase;
    private SelectCompanyUseCase selectCompanyUseCase;
    private ListUserCompaniesUseCase listUserCompaniesUseCase;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        loginUseCase = mock(LoginUseCase.class);
        selectCompanyUseCase = mock(SelectCompanyUseCase.class);
        listUserCompaniesUseCase = mock(ListUserCompaniesUseCase.class);
        mockMvc = standaloneSetup(new AuthController(loginUseCase, selectCompanyUseCase, listUserCompaniesUseCase))
                .build();
    }

    @Test
    void shouldLogin() throws Exception {
        UUID userId = UUID.randomUUID();
        when(loginUseCase.execute(any(LoginRequest.class)))
                .thenReturn(LoginResponse.bearer(
                        "jwt-token", 3600, new AuthUserResponse(userId, "user@example.com", "ADMIN"), List.of(), null));

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {
                                  "email": "user@example.com",
                                  "password": "secret123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andExpect(jsonPath("$.user.id").value(userId.toString()))
                .andExpect(jsonPath("$.user.email").value("user@example.com"))
                .andExpect(jsonPath("$.user.role").value("ADMIN"));
    }
}
