package com.thiagsilvadev.erpapi.common.web.exception;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.time.Clock;
import java.time.ZoneOffset;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.thiagsilvadev.erpapi.common.domain.exception.DomainErrorCode;
import com.thiagsilvadev.erpapi.common.domain.exception.DomainValidationException;
import com.thiagsilvadev.erpapi.support.FixedClockTestConfiguration;

class ValidationExceptionHandlerTest {

    private static final String NOT_BLANK_MESSAGE = "Title cannot be blank";

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ProblemDetailFactory problemDetails =
                new ProblemDetailFactory(Clock.fixed(FixedClockTestConfiguration.FIXED_INSTANT, ZoneOffset.UTC));
        mockMvc = standaloneSetup(new DummyController())
                .setControllerAdvice(new ValidationExceptionHandler(problemDetails))
                .build();
    }

    @Nested
    class MethodArgumentNotValid {
        @Test
        void shouldHandleValidationErrorsOnRequestBody() throws Exception {
            mockMvc.perform(post("/test-validation/dto")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"title\":\"\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.invalid_params[0].name").value("title"))
                    .andExpect(jsonPath("$.invalid_params[0].reason").value(NOT_BLANK_MESSAGE));
        }
    }

    @Nested
    class HttpMessageNotReadable {
        @Test
        void shouldHandleInvalidEnumValues() throws Exception {
            mockMvc.perform(post("/test-validation/enum")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"role\":\"ROLE_SUPERMAN\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("Malformed request body"))
                    .andExpect(jsonPath("$.invalid_params[0].name").value("role"))
                    .andExpect(jsonPath("$.invalid_params[0].reason", containsString("Accepted values are:")));
        }

        @ParameterizedTest
        @ValueSource(strings = {"{ title: ", "{\"title\":", ""})
        void shouldHandleMalformedJson(String malformedJson) throws Exception {
            mockMvc.perform(post("/test-validation/dto")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(malformedJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("Malformed request body"));
        }
    }

    @Nested
    class MethodArgumentTypeMismatch {
        @Test
        void shouldHandleWrongTypeInUrl() throws Exception {
            mockMvc.perform(get("/test-validation/mismatch/text"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("Type mismatch for parameter."))
                    .andExpect(jsonPath("$.invalid_params[0].name").value("id"))
                    .andExpect(jsonPath("$.invalid_params[0].reason", containsString("Long")));
        }
    }

    @Nested
    class RequestBinding {
        @Test
        void shouldHandleMissingRequestParam() throws Exception {
            mockMvc.perform(get("/test-validation/param"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("Missing required request component"))
                    .andExpect(jsonPath("$.invalid_params[0].name").value("val"));
        }

        @Test
        void shouldHandleMissingRequestHeader() throws Exception {
            mockMvc.perform(get("/test-validation/header"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.invalid_params[0].name").value("X-Custom-Header"));
        }
    }

    @Nested
    class HandlerMethodValidation {
        @Test
        void shouldHandleInvalidRequestParamValue() throws Exception {
            mockMvc.perform(get("/test-validation/param").param("val", "0"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.invalid_params[0].name").value("val"))
                    .andExpect(jsonPath("$.invalid_params[0].reason").value("must be greater than or equal to 1"));
        }
    }

    @Nested
    class DomainValidation {
        @Test
        void shouldHandleDomainValidationWithProblemDetailFactory() throws Exception {
            mockMvc.perform(get("/test-validation/domain"))
                    .andExpect(status().isUnprocessableContent())
                    .andExpect(jsonPath("$.status").value(HttpStatus.UNPROCESSABLE_CONTENT.value()))
                    .andExpect(jsonPath("$.detail").value("Country is required"))
                    .andExpect(jsonPath("$.type").value("http://localhost/errors/unprocessable-content"))
                    .andExpect(jsonPath("$.instance").value("/test-validation/domain"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        void shouldHandleDomainValidationErrorCode() throws Exception {
            mockMvc.perform(get("/test-validation/domain-code"))
                    .andExpect(status().isUnprocessableContent())
                    .andExpect(jsonPath("$.detail").value(DummyController.TestErrorCode.VALUE_MUST_BE_POSITIVE.name()))
                    .andExpect(jsonPath("$.error_code")
                            .value(DummyController.TestErrorCode.VALUE_MUST_BE_POSITIVE.name()));
        }
    }

    @RestController
    @RequestMapping("/test-validation")
    static class DummyController {

        record DummyDto(@NotBlank(message = NOT_BLANK_MESSAGE) String title) {}

        enum Role {
            ADMIN,
            USER
        }

        enum TestErrorCode implements DomainErrorCode {
            VALUE_MUST_BE_POSITIVE
        }

        record DummyEnumDto(Role role) {}

        @PostMapping("/dto")
        ResponseEntity<Void> testDto(@Valid @RequestBody DummyDto dto) {
            return ResponseEntity.noContent().build();
        }

        @PostMapping("/enum")
        ResponseEntity<Void> testEnum(@RequestBody DummyEnumDto dto) {
            return ResponseEntity.noContent().build();
        }

        @GetMapping("/mismatch/{id}")
        ResponseEntity<Void> testMismatch(@PathVariable Long id) {
            return ResponseEntity.noContent().build();
        }

        @GetMapping("/param")
        ResponseEntity<Void> testParam(@RequestParam @Min(1) Integer val) {
            return ResponseEntity.noContent().build();
        }

        @GetMapping("/header")
        ResponseEntity<Void> testHeader(@RequestHeader("X-Custom-Header") @NotBlank String header) {
            return ResponseEntity.noContent().build();
        }

        @GetMapping("/domain")
        ResponseEntity<Void> testDomain() {
            throw new DomainValidationException("Country is required");
        }

        @GetMapping("/domain-code")
        ResponseEntity<Void> testDomainCode() {
            throw new DomainValidationException(TestErrorCode.VALUE_MUST_BE_POSITIVE);
        }
    }
}
