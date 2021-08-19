package fr.gouv.tac.analytics.server.controller;

import com.nimbusds.jwt.JWTClaimsSet;
import fr.gouv.tac.analytics.server.test.IntegrationTest;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.time.Instant;

import static fr.gouv.tac.analytics.server.test.RestAssuredManager.defaultJwtClaims;
import static fr.gouv.tac.analytics.server.test.RestAssuredManager.givenJwt;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.WWW_AUTHENTICATE;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@IntegrationTest
class WebSecurityTest {

    @Test
    void unauthorized_on_missing_token() {
        given()
                .contentType(JSON)
                .post("/api/v1/analytics")

                .then()
                .statusCode(UNAUTHORIZED.value())
                .header(WWW_AUTHENTICATE, "Bearer");
    }

    @Test
    void unauthorized_on_token_having_expires_date_before_issues_date() {
        final var expired = Date.from(Instant.now().minus(10, MINUTES));
        givenJwt(defaultJwtClaims().expirationTime(expired))
                .contentType(JSON)
                .post("/api/v1/analytics")

                .then()
                .statusCode(UNAUTHORIZED.value())
                .header(WWW_AUTHENTICATE, startsWith("Bearer error=\"invalid_token\", error_description=\"An error occurred while attempting to decode the Jwt: expiresAt must be after issuedAt\","));
    }


    @Test
    void unauthorized_on_expired_token() {

        final var issueAt = Date.from(Instant.now().minus(10, MINUTES));
        final var expired = Date.from(Instant.now().minus(1, SECONDS));
        JWTClaimsSet.Builder jwtClaimsBuilder = defaultJwtClaims().expirationTime(expired).issueTime(issueAt);

        givenJwt(jwtClaimsBuilder)
                .contentType(JSON)
                .post("/api/v1/analytics")

                .then()
                .statusCode(UNAUTHORIZED.value())
                .header(WWW_AUTHENTICATE, startsWith("Bearer error=\"invalid_token\", error_description=\"An error occurred while attempting to decode the Jwt: Jwt expired at"));
    }


    @Test
    void unauthorized_on_invalid_token_signature() {
        given()
                .header(AUTHORIZATION, "Bearer eyJhbGciOiJSUzI1NiJ9.eyJleHAiOjE2MjIyMzkwODAsImlhdCI6MTYyMjIzOTY4MCwianRpIjoiMWEyYmNlYTctZGMxMS00NTYwLWExZTYtNWYwNDY5OGZhMGZlIn0.INVALID_SIGNATURE")
                .contentType(JSON)
                .post("/api/v1/analytics")

                .then()
                .statusCode(UNAUTHORIZED.value())
                .header(WWW_AUTHENTICATE, startsWith("Bearer error=\"invalid_token\", error_description=\"An error occurred while attempting to decode the Jwt: Signed JWT rejected: Invalid signature\","));
    }

    @Test
    void there_is_no_situation_where_a_forbidden_error_can_occurs() {
        // There is no test for 'FORBIDDEN' status because there is no authorities and no access rules.
        // Such a test has no meaning.
    }
}
