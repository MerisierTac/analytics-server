package fr.gouv.tac.analytics.server.controller;

import fr.gouv.tac.analytics.server.api.model.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolationException;

import java.time.OffsetDateTime;

@Slf4j
@RestControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = AuthenticationException.class)
    public ResponseEntity<Object> exception(final AuthenticationException e) {
        return errorVoBuilder(e, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = ConstraintViolationException.class)
    public ResponseEntity<Object> exception(final ConstraintViolationException e) {
        return errorVoBuilder(e, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Object> exception(final Exception e) {
        log.warn("Unexpected error :", e);
        return errorVoBuilder(e, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body, HttpHeaders headers,
            HttpStatus status, WebRequest request) {
        return errorVoBuilder(ex, status);
    }

    private ResponseEntity<Object> errorVoBuilder(final Exception e, final HttpStatus httpStatus) {
        final ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage(e.getMessage());
        errorResponse.setTimestamp(OffsetDateTime.now());
        return ResponseEntity.status(httpStatus).body(errorResponse);
    }
}
