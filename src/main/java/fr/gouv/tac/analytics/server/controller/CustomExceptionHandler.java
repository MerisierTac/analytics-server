package fr.gouv.tac.analytics.server.controller;

import java.time.OffsetDateTime;

import javax.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import fr.gouv.tac.analytics.server.api.model.ErrorResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(value = AuthenticationException.class)
    public ResponseEntity<ErrorResponse> exception(final AuthenticationException e) {
        return errorVoBuilder(e, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> exception(final MethodArgumentNotValidException e) {
        return errorVoBuilder(e, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> exception(final MissingServletRequestParameterException e) {
        return errorVoBuilder(e, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> exception(final ConstraintViolationException e) {
        return errorVoBuilder(e, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrorResponse> exception(final Exception e) {
        log.warn("Unexpected error :", e);
        return errorVoBuilder(e, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> errorVoBuilder(final Exception e, final HttpStatus httpStatus) {
        final ErrorResponse errorResponse = new ErrorResponse();
                errorResponse.setMessage(e.getMessage());
                errorResponse.setTimestamp(OffsetDateTime.now());
        return ResponseEntity.status(httpStatus).body(errorResponse);
    }

}
