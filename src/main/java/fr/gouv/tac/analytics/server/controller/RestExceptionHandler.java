package fr.gouv.tac.analytics.server.controller;

import fr.gouv.tac.analytics.server.api.model.ErrorResponse;
import fr.gouv.tac.analytics.server.api.model.ErrorResponseErrors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;

import java.time.OffsetDateTime;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestControllerAdvice
@RequiredArgsConstructor
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    private final HttpServletRequest servletRequest;

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatus status, WebRequest request) {
        final var fieldErrors = ex.getFieldErrors().stream()
                .map(err -> ErrorResponseErrors.builder()
                        .field(err.getField())
                        .code(err.getCode())
                        .message(err.getDefaultMessage())
                        .build());
        final var globalErrors = ex.getGlobalErrors().stream()
                .map(err -> ErrorResponseErrors.builder()
                        .field("")
                        .code(err.getCode())
                        .message(err.getDefaultMessage())
                        .build());
        final var errorResponseBody = ErrorResponse.builder()
                .status(BAD_REQUEST.value())
                .error(BAD_REQUEST.getReasonPhrase())
                .message("Request body contains invalid attributes")
                .timestamp(OffsetDateTime.now())
                .path(servletRequest.getRequestURI())
                .errors(Stream.concat(fieldErrors, globalErrors)
                        .collect(toList()))
                .build();
        return ResponseEntity
                .status(BAD_REQUEST.value())
                .body(errorResponseBody);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handle(ConstraintViolationException ex, HttpServletRequest request) {
        final var validationErrors = ex.getConstraintViolations().stream()
                .map(err -> ErrorResponseErrors.builder()
                        .field(err.getPropertyPath().toString())
                        .code(err.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName())
                        .message(err.getMessage())
                        .build())
                .collect(toList());
        final var errorResponseBody = ErrorResponse.builder()
                .status(BAD_REQUEST.value())
                .error(BAD_REQUEST.getReasonPhrase())
                .message("Request body contains invalid attributes")
                .timestamp(OffsetDateTime.now())
                .path(request.getRequestURI())
                .errors(validationErrors)
                .build();
        return ResponseEntity
                .status(BAD_REQUEST.value())
                .body(errorResponseBody);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
            HttpStatus status, WebRequest request) {
        super.handleExceptionInternal(ex, body, headers, status, request);
        final var errorResponseBody = ErrorResponse.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(ex.getMessage())
                .timestamp(OffsetDateTime.now())
                .path(servletRequest.getRequestURI());
        return ResponseEntity
                .status(status)
                .body(errorResponseBody.build());
    }
}
