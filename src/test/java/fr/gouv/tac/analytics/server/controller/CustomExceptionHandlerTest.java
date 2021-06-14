package fr.gouv.tac.analytics.server.controller;

import fr.gouv.tac.analytics.server.api.model.ErrorResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.validation.ConstraintViolationException;

import java.time.ZonedDateTime;

@ExtendWith(SpringExtension.class)
public class CustomExceptionHandlerTest {

    @Mock
    private ConstraintViolationException constraintViolationException;

    @InjectMocks
    private CustomExceptionHandler customExceptionHandler;

    @Test
    public void shouldManageAuthenticationException() {
        final OAuth2AuthenticationException oAuth2AuthenticationException = new OAuth2AuthenticationException(
                new OAuth2Error("someCode"), "someMessage"
        );

        final ResponseEntity<Object> result = customExceptionHandler.exception(oAuth2AuthenticationException);
        checkResult(result, HttpStatus.UNAUTHORIZED, oAuth2AuthenticationException.getMessage(), ZonedDateTime.now());
    }

    @Test
    public void shouldManageConstraintViolationException() {
        final String message = "error message";
        Mockito.when(constraintViolationException.getMessage()).thenReturn(message);

        final ResponseEntity<Object> result = customExceptionHandler.exception(constraintViolationException);
        checkResult(result, HttpStatus.BAD_REQUEST, message, ZonedDateTime.now());
    }

    @Test
    public void shouldManageEveryOtherException() {
        final Exception exception = new Exception("someMessage");

        final ResponseEntity<Object> result = customExceptionHandler.exception(exception);
        checkResult(result, HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage(), ZonedDateTime.now());
    }

    private void checkResult(ResponseEntity<Object> responseToCheck, HttpStatus expectedStatus, String expectedMessage,
            ZonedDateTime expectedTimestamp) {
        ErrorResponse errorResponse = (ErrorResponse) responseToCheck.getBody();
        Assertions.assertThat(responseToCheck.getStatusCode()).isEqualTo(expectedStatus);
        Assertions.assertThat(errorResponse.getMessage()).isEqualTo(expectedMessage);
        Assertions.assertThat(errorResponse.getTimestamp().toZonedDateTime())
                .isEqualToIgnoringSeconds(expectedTimestamp);
    }
}
