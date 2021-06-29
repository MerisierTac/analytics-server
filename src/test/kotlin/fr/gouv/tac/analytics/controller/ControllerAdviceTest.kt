package fr.gouv.tac.analytics.controller

import fr.gouv.tac.analytics.test.IntegrationTest
import fr.gouv.tac.analytics.test.TemporalMatchers.isStringDateBetweenNowAndTenSecondsAgo
import fr.gouv.tac.analytics.test.RestAssuredManager.Companion.givenAuthenticated
import io.restassured.http.ContentType
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

@IntegrationTest
internal class ControllerAdviceTest {
    @Test
    fun should_return_not_found_on_unknown_resource() {
        givenAuthenticated()
            .get("/api/v1/unknown")
            .then()
            .contentType(ContentType.JSON)
            .statusCode(HttpStatus.NOT_FOUND.value())
            .body("status", Matchers.equalTo(404))
            .body("error", Matchers.equalTo("Not Found"))
            .body("message", Matchers.equalTo("No message available"))
            .body("timestamp", isStringDateBetweenNowAndTenSecondsAgo)
            .body("path", Matchers.equalTo("/api/v1/unknown"))
    }
}