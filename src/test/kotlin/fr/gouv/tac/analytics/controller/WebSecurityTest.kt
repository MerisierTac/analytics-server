package fr.gouv.tac.analytics.controller

import fr.gouv.tac.analytics.test.IntegrationTest
import fr.gouv.tac.analytics.test.RestAssuredManager.Companion.defaultJwtClaims
import fr.gouv.tac.analytics.test.RestAssuredManager.Companion.givenJwt
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import java.sql.Date
import java.time.Instant
import java.time.temporal.ChronoUnit

@IntegrationTest
internal class WebSecurityTest {
    @Test
    fun unauthorized_on_missing_token() {
        RestAssured.given()
            .contentType(ContentType.JSON)
            .post("/api/v1/analytics")
            .then()
            .statusCode(HttpStatus.UNAUTHORIZED.value())
            .header(HttpHeaders.WWW_AUTHENTICATE, "Bearer")
    }

    @Test
    fun unauthorized_on_expired_token() {
        val expired = Date.from(Instant.now().minus(10, ChronoUnit.MINUTES))
        givenJwt(defaultJwtClaims().expirationTime(expired))
            .contentType(ContentType.JSON)
            .post("/api/v1/analytics")
            .then()
            .statusCode(HttpStatus.UNAUTHORIZED.value())
            .header(
                HttpHeaders.WWW_AUTHENTICATE,
                Matchers.startsWith("Bearer error=\"invalid_token\", error_description=\"An error occurred while attempting to decode the Jwt: expiresAt must be after issuedAt\",")
            )
    }

    @Test
    fun unauthorized_on_invalid_token_signature() {
        RestAssured.given()
            .header(
                HttpHeaders.AUTHORIZATION,
                "Bearer eyJhbGciOiJSUzI1NiJ9.eyJleHAiOjE2MjIyMzkwODAsImlhdCI6MTYyMjIzOTY4MCwianRpIjoiMWEyYmNlYTctZGMxMS00NTYwLWExZTYtNWYwNDY5OGZhMGZlIn0.INVALID_SIGNATURE"
            )
            .contentType(ContentType.JSON)
            .post("/api/v1/analytics")
            .then()
            .statusCode(HttpStatus.UNAUTHORIZED.value())
            .header(
                HttpHeaders.WWW_AUTHENTICATE,
                Matchers.startsWith("Bearer error=\"invalid_token\", error_description=\"An error occurred while attempting to decode the Jwt: Signed JWT rejected: Invalid signature\",")
            )
    }

    @Test
    fun there_is_no_situation_where_a_forbidden_error_can_occurs() {
        // There is no test for 'FORBIDDEN' status because there is no authorities and no access rules.
        // Such a test has no meaning.
    }
}
