package fr.gouv.tac.analytics.controller

import fr.gouv.tac.analytics.test.IntegrationTest
import fr.gouv.tac.analytics.test.RestAssuredManager.Companion.givenAuthenticated
import fr.gouv.tac.analytics.test.TemporalMatchers.isStringDateBetweenNowAndTenSecondsAgo
import io.restassured.http.ContentType.JSON
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus.NOT_FOUND

@IntegrationTest
class ControllerAdviceTest {
    @Test
    fun should_return_not_found_on_unknown_resource() {
        givenAuthenticated()
            .get("/api/v1/unknown")
            .then()
            .contentType(JSON)
            .statusCode(NOT_FOUND.value())
            .body("status", equalTo(404))
            .body("error", equalTo("Not Found"))
            .body("message", equalTo("No message available"))
            .body("timestamp", isStringDateBetweenNowAndTenSecondsAgo())
            .body("path", equalTo("/api/v1/unknown"))
    }
}
