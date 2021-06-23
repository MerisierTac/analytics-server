package fr.gouv.tac.analytics.server.controller;

import fr.gouv.tac.analytics.server.test.IntegrationTest;
import org.junit.jupiter.api.Test;

import static fr.gouv.tac.analytics.server.test.TemporalMatchers.isStringDateBetweenNowAndTenSecondsAgo;
import static fr.gouv.tac.analytics.server.test.RestAssuredManager.givenAuthenticated;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@IntegrationTest
class ControllerAdviceTest {

    @Test
    void should_return_not_found_on_unknown_resource() {
        givenAuthenticated()
                .get("/api/v1/unknown")

                .then()
                .contentType(JSON)
                .statusCode(NOT_FOUND.value())
                .body("status", equalTo(404))
                .body("error", equalTo("Not Found"))
                .body("message", equalTo("No message available"))
                .body("timestamp", isStringDateBetweenNowAndTenSecondsAgo())
                .body("path", equalTo("/api/v1/unknown"));
    }
}
