package com.bolsillo.ahorro.interfaces.rest;

import com.bolsillo.ahorro.domain.exception.ContributionNotAllowedException;
import com.bolsillo.ahorro.domain.exception.DomainException;
import com.bolsillo.ahorro.domain.exception.GoalNotFoundException;
import java.util.stream.Collectors;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    static final String CONCURRENT_MODIFICATION = "CONCURRENT_MODIFICATION";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return problem(HttpStatus.BAD_REQUEST, "Validation failed", detail, VALIDATION_ERROR);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail handleUnreadable(HttpMessageNotReadableException ex) {
        return problem(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                "Request body is not valid JSON",
                VALIDATION_ERROR);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Validation failed", ex.getMessage(), VALIDATION_ERROR);
    }

    @ExceptionHandler(GoalNotFoundException.class)
    ProblemDetail handleNotFound(GoalNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Goal not found", ex.getMessage(), ex.code());
    }

    @ExceptionHandler(ContributionNotAllowedException.class)
    ProblemDetail handleContributionNotAllowed(ContributionNotAllowedException ex) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Contribution not allowed", ex.getMessage(), ex.code());
    }

    @ExceptionHandler(DomainException.class)
    ProblemDetail handleDomain(DomainException ex) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Business rule violated", ex.getMessage(), ex.code());
    }

    @ExceptionHandler({OptimisticLockingFailureException.class, ObjectOptimisticLockingFailureException.class})
    ProblemDetail handleConflict(OptimisticLockingFailureException ex) {
        return problem(
                HttpStatus.CONFLICT,
                "Concurrent modification",
                "The goal was updated by another request. Reload and try again.",
                CONCURRENT_MODIFICATION);
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail, String code) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setProperty("code", code);
        return problem;
    }
}
