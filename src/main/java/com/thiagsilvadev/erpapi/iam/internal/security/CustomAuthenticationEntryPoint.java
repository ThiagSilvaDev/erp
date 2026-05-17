package com.thiagsilvadev.erpapi.iam.internal.security;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.thiagsilvadev.erpapi.common.web.exception.ProblemDetailFactory;

import tools.jackson.databind.ObjectMapper;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ProblemDetailFactory problemDetailFactory;
    private final ObjectMapper objectMapper;

    public CustomAuthenticationEntryPoint(ProblemDetailFactory problemDetailFactory, ObjectMapper objectMapper) {
        this.problemDetailFactory = problemDetailFactory;
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            @NonNull HttpServletRequest request, HttpServletResponse response, @NonNull AuthenticationException authException)
            throws IOException {
        ProblemDetail problemDetail = problemDetailFactory.create(
                HttpStatus.UNAUTHORIZED, "Authentication is required to access this resource.", request, null);

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(problemDetail));
    }
}
