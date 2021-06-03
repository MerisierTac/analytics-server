package fr.gouv.tac.analytics.server.controller;

import fr.gouv.tac.analytics.server.api.model.ErrorResponse;
import fr.gouv.tac.analytics.server.api.model.ErrorResponseErrors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestControllerAdvice
@RequiredArgsConstructor
public class RestExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handle(MethodArgumentNotValidException ex, HttpServletRequest request) {
        final var validationErrors = ex.getFieldErrors().stream()
                .map(err -> ErrorResponseErrors.builder()
                        .field(err.getField())
                        .code(err.getCode())
                        .message(err.getDefaultMessage())
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
}
