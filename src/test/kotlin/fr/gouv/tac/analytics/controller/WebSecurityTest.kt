package fr.gouv.tac.analytics.controller

import fr.gouv.tac.analytics.test.IntegrationTest
import fr.gouv.tac.analytics.test.RestAssuredManager.Companion.defaultJwtClaims
import fr.gouv.tac.analytics.test.RestAssuredManager.Companion.givenJwt
import io.restassured.RestAssured.given
import io.restassured.http.ContentType.JSON
import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpHeaders.WWW_AUTHENTICATE
import org.springframework.http.HttpStatus.UNAUTHORIZED
import java.sql.Date
import java.time.Instant
import java.time.temporal.ChronoUnit.MINUTES
import java.time.temporal.ChronoUnit.SECONDS

@IntegrationTest
class WebSecurityTest {
    @Test
    fun unauthorized_on_missing_token() {
        given()
            .contentType(JSON)
            .post("/api/v1/analytics")
            .then()
            .statusCode(UNAUTHORIZED.value())
            .header(WWW_AUTHENTICATE, "Bearer")
    }

    @Test
    fun unauthorized_on_token_having_expires_date_before_issues_date() {
        val expired = Date.from(Instant.now().minus(10, MINUTES))
        givenJwt(defaultJwtClaims().expirationTime(expired))
            .contentType(JSON)
            .post("/api/v1/analytics")
            .then()
            .statusCode(UNAUTHORIZED.value())
            .header(WWW_AUTHENTICATE, startsWith("Bearer error=\"invalid_token\", error_description=\"An error occurred while attempting to decode the Jwt: expiresAt must be after issuedAt\","))
    }

    @Test
    fun unauthorized_on_expired_token() {
        val issueAt = Date.from(Instant.now().minus(10, MINUTES))
        val expired = Date.from(Instant.now().minus(1, SECONDS))
        val jwtClaimsBuilder = defaultJwtClaims().expirationTime(expired).issueTime(issueAt)
        givenJwt(jwtClaimsBuilder)
            .contentType(JSON)
            .post("/api/v1/analytics")
            .then()
            .statusCode(UNAUTHORIZED.value())
            .header(WWW_AUTHENTICATE, startsWith("Bearer error=\"invalid_token\", error_description=\"An error occurred while attempting to decode the Jwt: Jwt expired at"))
    }

    @Test
    fun unauthorized_on_invalid_token_signature() {
        given()
            .header(AUTHORIZATION, "Bearer eyJhbGciOiJSUzI1NiJ9.eyJleHAiOjE2MjIyMzkwODAsImlhdCI6MTYyMjIzOTY4MCwianRpIjoiMWEyYmNlYTctZGMxMS00NTYwLWExZTYtNWYwNDY5OGZhMGZlIn0.INVALID_SIGNATURE")
            .contentType(JSON)
            .post("/api/v1/analytics")
            .then()
            .statusCode(UNAUTHORIZED.value())
            .header(WWW_AUTHENTICATE, startsWith("Bearer error=\"invalid_token\", error_description=\"An error occurred while attempting to decode the Jwt: Signed JWT rejected: Invalid signature\","))
    }

    @Test
    fun there_is_no_situation_where_a_forbidden_error_can_occurs() {
        // There is no test for 'FORBIDDEN' status because there is no authorities and no access rules.
        // Such a test has no meaning.
    }
}
