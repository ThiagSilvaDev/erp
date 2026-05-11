package com.thiagsilvadev.erpapi.common.api;

import java.util.Map;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.thiagsilvadev.erpapi.common.exception.BusinessRuleException;
import com.thiagsilvadev.erpapi.common.exception.ConflictException;

@RestControllerAdvice
@Order(2)
public class DomainExceptionHandler {

    private final ProblemDetailFactory problemDetailFactory;

    public DomainExceptionHandler(ProblemDetailFactory problemDetailFactory) {
        this.problemDetailFactory = problemDetailFactory;
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ProblemDetail> handleBusinessRule(BusinessRuleException exception) {
        return build(exception.getStatus(), exception.getMessage(), exception.getProperties());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ProblemDetail> handleConflict(ConflictException exception) {
        return build(HttpStatus.CONFLICT, exception.getMessage(), exception.getProperties());
    }

    private ResponseEntity<ProblemDetail> build(HttpStatus status, String message, Map<String, ?> properties) {
        ProblemDetail problemDetail = problemDetailFactory.create(status, message, properties);
        return ResponseEntity.status(status).body(problemDetail);
    }
}
