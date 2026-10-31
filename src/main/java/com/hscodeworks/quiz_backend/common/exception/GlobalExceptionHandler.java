package com.hscodeworks.quiz_backend.common.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // --------------------------- Custom exceptions ---------------------------

    @ExceptionHandler( ResourceNotFoundException.class )
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail( HttpStatus.NOT_FOUND, ex.getMessage() );
    }

    @ExceptionHandler( ConflictException.class )
    public ProblemDetail handleConflict(ConflictException ex) {
        return ProblemDetail.forStatusAndDetail( HttpStatus.CONFLICT, ex.getMessage() );
    }

    @ExceptionHandler( BadRequestException.class )
    public ProblemDetail handleBadRequest(BadRequestException ex) {
        return ProblemDetail.forStatusAndDetail( HttpStatus.BAD_REQUEST, ex.getMessage() );
    }

    @ExceptionHandler(ImageStorageException.class)
    public ProblemDetail handleImageStorage(ImageStorageException ex) {
        return ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_GATEWAY,
            "The image storage service is unavailable or rejected the upload. Please try again later."
        );
    }


    // --------------------------- Database Safety Net ---------------------------

    @ExceptionHandler( DataIntegrityViolationException.class )
    public ProblemDetail handleIntegrity(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation", ex);

        return ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            "This operation conflicts with existing data (for example, deleting a category that still has questions)."
        );
    }

    // --------------------------- Catch-all for unexpected errors ---------------------------

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected( Exception ex ) {
        log.error("Unexpected error", ex);

        return ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Something went wrong on the server. Please try again later."
        );
    }


    // --------------------------- Overriding Spring's built-in handling ---------------------------

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, 
            HttpHeaders headers, 
            HttpStatusCode status, 
            WebRequest request
    ) {

        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                             .forEach( error -> errors.putIfAbsent( error.getField(), error.getDefaultMessage() ));

        ProblemDetail problem = ProblemDetail.forStatusAndDetail( HttpStatus.BAD_REQUEST, "Validation failed" );
        problem.setProperty("errors", errors);
        return handleExceptionInternal(ex, problem, headers, status, request);
    }


    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable( 
            HttpMessageNotReadableException ex, 
            HttpHeaders headers, 
            HttpStatusCode status, 
            WebRequest request
    ) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Malformed request body. Check the JSON syntax (and enum values such as 'type' if adding a question)."
        );
        
        return handleExceptionInternal(ex, problem, headers, status, request);
    }
}