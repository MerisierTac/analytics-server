package fr.gouv.tac.analytics.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.tac.analytics.server.test.ExampleData;
import fr.gouv.tac.analytics.server.test.IntegrationTest;
import fr.gouv.tac.analytics.server.test.KafkaManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static fr.gouv.tac.analytics.server.test.KafkaRecordAssert.assertThat;
import static fr.gouv.tac.analytics.server.test.RestAssuredManager.givenAuthenticated;
import static fr.gouv.tac.analytics.server.test.TemporalMatchers.isStringDateBetweenNowAndTenSecondsAgo;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@IntegrationTest
class AnalyticsControllerCreateTest {

    @Test
    void should_send_a_create_message_in_kafka() {
        final var analyticsRequest = ExampleData.analyticsRequest()
                .build();
        givenAuthenticated()
                .contentType(APPLICATION_JSON_VALUE)
                .body(analyticsRequest)
                .post("/api/v1/analytics")

                .then()
                .statusCode(OK.value())
                .body(emptyString());

        assertThat(KafkaManager.getSingleRecord("dev.analytics.cmd.create"))
                .hasNoHeader("__TypeId__")
                .hasNoKey()
                .hasJsonValue("creationDate", isStringDateBetweenNowAndTenSecondsAgo())
                .hasJsonValue("infos.os", "Android")
                .hasJsonValue("infos.type", 0)
                .hasJsonValue("infos.load", 1.03)
                .hasJsonValue("infos.root", false)
                .hasJsonValue("events[0].name", "eventName1")
                .hasJsonValue("events[0].timestamp", "2020-12-17T10:59:17.123Z")
                .hasJsonValue("events[0].desc", "event1 description")
                .hasJsonValue("events[1].name", "eventName2")
                .hasJsonValue("events[1].timestamp", "2020-12-17T10:59:17.123Z")
                .hasJsonValue("errors[0].name", "errorName1")
                .hasJsonValue("errors[0].timestamp", "2020-12-17T10:59:17.123Z")
                .hasJsonValue("errors[1].name", "errorName2")
                .hasJsonValue("errors[1].timestamp", "2020-12-17T10:59:17.123Z")
                .hasJsonValue("errors[1].desc", "error2 description");
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
                .body("timestamp", isStringDateBetweenNowAndTenSecondsAgo())
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
                .body("timestamp", isStringDateBetweenNowAndTenSecondsAgo())
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
                .body("timestamp", isStringDateBetweenNowAndTenSecondsAgo())
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
                .body("message", startsWith(
                        "JSON parse error: Cannot deserialize value of type `java.time.OffsetDateTime` from String \"2021-06-02T22:47:02.388 PMZ\""))
                .body("timestamp", isStringDateBetweenNowAndTenSecondsAgo())
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
                .body("timestamp", isStringDateBetweenNowAndTenSecondsAgo())
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
                .body("timestamp", isStringDateBetweenNowAndTenSecondsAgo())
                .body("path", equalTo("/api/v1/analytics"))
                .body("errors", contains(Map.of(
                        "field", "errors[0].timestamp",
                        "code", "NotNull",
                        "message", "must not be null"
                )));
    }

}
