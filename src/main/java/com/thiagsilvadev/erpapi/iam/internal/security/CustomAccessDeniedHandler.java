package com.thiagsilvadev.erpapi.iam.internal.security;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.thiagsilvadev.erpapi.common.web.exception.ProblemDetailFactory;

import tools.jackson.databind.ObjectMapper;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ProblemDetailFactory problemDetailFactory;
    private final ObjectMapper objectMapper;

    public CustomAccessDeniedHandler(ProblemDetailFactory problemDetailFactory, ObjectMapper objectMapper) {
        this.problemDetailFactory = problemDetailFactory;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(
            @NonNull HttpServletRequest request, HttpServletResponse response, @NonNull AccessDeniedException accessDeniedException)
            throws IOException {
        ProblemDetail problemDetail =
                problemDetailFactory.create(HttpStatus.FORBIDDEN, "Access is denied for this resource.", request, null);

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(problemDetail));
    }
}
