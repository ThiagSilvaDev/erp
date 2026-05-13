package com.thiagsilvadev.erpapi.iam.internal.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.thiagsilvadev.erpapi.iam.internal.dto.LoginRequest;
import com.thiagsilvadev.erpapi.iam.internal.dto.LoginResponse;
import com.thiagsilvadev.erpapi.iam.internal.usecase.LoginUseCase;

class AuthControllerTest {

    private LoginUseCase loginUseCase;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        loginUseCase = org.mockito.Mockito.mock(LoginUseCase.class);
        mockMvc = standaloneSetup(new AuthController(loginUseCase)).build();
    }

    @Test
    void shouldLogin() throws Exception {
        UUID userId = UUID.randomUUID();
        when(loginUseCase.execute(any(LoginRequest.class)))
                .thenReturn(new LoginResponse("jwt-token", "Bearer", 3600, userId, "ADMIN"));

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
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }
}
