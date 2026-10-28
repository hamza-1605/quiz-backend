package com.hscodeworks.quiz_backend.common.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler( ResourceNotFoundException.class )
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail( HttpStatus.NOT_FOUND, ex.getMessage() );
    }


    @ExceptionHandler( ConflictException.class )
    public ProblemDetail handleConflict(ConflictException ex) {
        return ProblemDetail.forStatusAndDetail( HttpStatus.CONFLICT, ex.getMessage() );
    }


    @ExceptionHandler( MethodArgumentNotValidException.class )
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));

        ProblemDetail problem = ProblemDetail.forStatusAndDetail( HttpStatus.BAD_REQUEST, "Validation failed" );
        problem.setProperty("errors", errors);
        return problem;
    }


    @ExceptionHandler( BadRequestException.class )
    public ProblemDetail handleBadRequest(BadRequestException ex) {
        return ProblemDetail.forStatusAndDetail( HttpStatus.BAD_REQUEST, ex.getMessage() );
    }


    @ExceptionHandler( HttpMessageNotReadableException.class )
    public ProblemDetail handleUnreadableBody(HttpMessageNotReadableException ex) {
        return ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Malformed request body. Check the JSON syntax and enum values such as 'type'."
        );
    }


    @ExceptionHandler( DataIntegrityViolationException.class )
    public ProblemDetail handleIntegrity(DataIntegrityViolationException ex) {
        return ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            "This operation conflicts with existing data (for example, deleting a category that still has questions)."
        );
    }
}