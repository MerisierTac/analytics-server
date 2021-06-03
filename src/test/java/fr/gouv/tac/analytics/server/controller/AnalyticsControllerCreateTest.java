package fr.gouv.tac.analytics.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.tac.analytics.server.service.kafka.model.AnalyticsCreation;
import fr.gouv.tac.analytics.server.test.ExampleData;
import fr.gouv.tac.analytics.server.test.IntegrationTest;
import fr.gouv.tac.analytics.server.test.KafkaManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static fr.gouv.tac.analytics.server.test.RestAssuredMatchers.isStringDateBetweenNowAndTenSecondAgo;
import static fr.gouv.tac.analytics.server.test.RestAssuredManager.givenAuthenticated;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@IntegrationTest
class AnalyticsControllerCreateTest {

    @Autowired
    AnalyticsMapper analyticsMapper;

    @Test
    void should_send_a_create_message_in_kafka() {
        final var analyticsRequest = ExampleData.analyticsRequest()
                .build();
        final var expectedMessage = analyticsMapper.map(analyticsRequest);

        givenAuthenticated()
                .contentType(APPLICATION_JSON_VALUE)
                .body(analyticsRequest)
                .post("/api/v1/analytics")

                .then()
                .statusCode(OK.value())
                .body(emptyString());

        assertThat(KafkaManager.awaitMessageValues(AnalyticsCreation.class))
                .containsExactly(expectedMessage);
    }

    @Test
    void bad_request_on_null_installationUuid() {
        final var analyticsRequest = ExampleData.analyticsRequest()
                .installationUuid(null)
                .build();

        givenAuthenticated()
                .contentType(APPLICATION_JSON_VALUE)
                .body(analyticsRequest)
                .post("/api/v1/analytics")

                .then()
                .statusCode(BAD_REQUEST.value())
                .body("status", equalTo(400))
                .body("error", equalTo("Bad Request"))
                .body("message", equalTo("Request body contains invalid attributes"))
                .body("timestamp", isStringDateBetweenNowAndTenSecondAgo())
                .body("path", equalTo("/api/v1/analytics"))
                .body("errors", contains(Map.of(
                        "field", "installationUuid",
                        "code", "NotNull",
                        "message", "must not be null"
                )));
    }

    @Test
    void bad_request_on_empty_event_name() {
        final var analyticsRequest = ExampleData.analyticsRequest()
                .build();
        analyticsRequest.getEvents().get(0).setName(null);

        givenAuthenticated()
                .contentType(APPLICATION_JSON_VALUE)
                .body(analyticsRequest)
                .post("/api/v1/analytics")

                .then()
                .statusCode(BAD_REQUEST.value())
                .body("status", equalTo(400))
                .body("error", equalTo("Bad Request"))
                .body("message", equalTo("Request body contains invalid attributes"))
                .body("timestamp", isStringDateBetweenNowAndTenSecondAgo())
                .body("path", equalTo("/api/v1/analytics"))
                .body("errors", contains(Map.of(
                        "field", "events[0].name",
                        "code", "NotNull",
                        "message", "must not be null"
                )));
    }

    @Test
    void bad_request_on_empty_event_timestamp() {
        final var analyticsRequest = ExampleData.analyticsRequest()
                .build();
        analyticsRequest.getEvents().get(0).setTimestamp(null);

        givenAuthenticated()
                .contentType(APPLICATION_JSON_VALUE)
                .body(analyticsRequest)
                .post("/api/v1/analytics")

                .then()
                .statusCode(BAD_REQUEST.value())
                .body("status", equalTo(400))
                .body("error", equalTo("Bad Request"))
                .body("message", equalTo("Request body contains invalid attributes"))
                .body("timestamp", isStringDateBetweenNowAndTenSecondAgo())
                .body("path", equalTo("/api/v1/analytics"))
                .body("errors", contains(Map.of(
                        "field", "events[0].timestamp",
                        "code", "NotNull",
                        "message", "must not be null"
                )));
    }

    @Test
    void bad_request_on_malformed_event_timestamp(@Autowired ObjectMapper objectMapper) {

        final var eventWithMalformedTimestamp = Map.of(
                "name", "eventName",
                "timestamp", "2021-06-02T22:47:02.388 PMZ"
        );

        givenAuthenticated()
                .contentType(APPLICATION_JSON_VALUE)
                .body(Map.of(
                        "installationUuid", UUID.randomUUID(),
                        "events", List.of(eventWithMalformedTimestamp),
                        "errors", List.of()
                ))
                .post("/api/v1/analytics")

                .then()
                .statusCode(BAD_REQUEST.value())
                .body("status", equalTo(400))
                .body("error", equalTo("Bad Request"))
                .body("message", startsWith("JSON parse error: Cannot deserialize value of type `java.time.OffsetDateTime` from String \"2021-06-02T22:47:02.388 PMZ\""))
                .body("timestamp", isStringDateBetweenNowAndTenSecondAgo())
                .body("path", equalTo("/api/v1/analytics"));
    }

    @Test
    void bad_request_on_empty_error_name() {
        final var analyticsRequest = ExampleData.analyticsRequest()
                .build();
        analyticsRequest.getErrors().get(0).setName(null);

        givenAuthenticated()
                .contentType(APPLICATION_JSON_VALUE)
                .body(analyticsRequest)
                .post("/api/v1/analytics")

                .then()
                .statusCode(BAD_REQUEST.value())
                .body("status", equalTo(400))
                .body("error", equalTo("Bad Request"))
                .body("message", equalTo("Request body contains invalid attributes"))
                .body("timestamp", isStringDateBetweenNowAndTenSecondAgo())
                .body("path", equalTo("/api/v1/analytics"))
                .body("errors", contains(Map.of(
                        "field", "errors[0].name",
                        "code", "NotNull",
                        "message", "must not be null"
                )));
    }

    @Test
    void bad_request_on_empty_error_timestamp() {
        final var analyticsRequest = ExampleData.analyticsRequest()
                .build();
        analyticsRequest.getErrors().get(0).setTimestamp(null);

        givenAuthenticated()
                .contentType(APPLICATION_JSON_VALUE)
                .body(analyticsRequest)
                .post("/api/v1/analytics")

                .then()
                .statusCode(BAD_REQUEST.value())
                .body("status", equalTo(400))
                .body("error", equalTo("Bad Request"))
                .body("message", equalTo("Request body contains invalid attributes"))
                .body("timestamp", isStringDateBetweenNowAndTenSecondAgo())
                .body("path", equalTo("/api/v1/analytics"))
                .body("errors", contains(Map.of(
                        "field", "errors[0].timestamp",
                        "code", "NotNull",
                        "message", "must not be null"
                )));
    }

}
