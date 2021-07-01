package fr.gouv.tac.analytics.controller

import com.fasterxml.jackson.databind.ObjectMapper
import fr.gouv.tac.analytics.api.model.TimestampedEvent
import fr.gouv.tac.analytics.test.ExampleData
import fr.gouv.tac.analytics.test.IntegrationTest
import fr.gouv.tac.analytics.test.KafkaManager
import fr.gouv.tac.analytics.test.KafkaRecordAssert.Companion.assertThat
import fr.gouv.tac.analytics.test.RestAssuredManager.Companion.givenAuthenticated
import fr.gouv.tac.analytics.test.TemporalMatchers.isStringDateBetweenNowAndTenSecondsAgo
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import java.time.OffsetDateTime
import java.util.*

@IntegrationTest
internal class AnalyticsControllerCreateTest {
    @Test
    fun should_send_a_create_message_in_kafka() {
        val analyticsRequest = ExampleData.analyticsRequest()
        givenAuthenticated()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(analyticsRequest)
            .post("/api/v1/analytics")
            .then()
            .statusCode(HttpStatus.OK.value())
            .body(Matchers.emptyString())
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
            .hasJsonValue("errors[1].desc", "error2 description")
    }

    @Test
    fun bad_request_on_empty_installationUuid() {
        val analyticsRequest = ExampleData.analyticsRequest().copy(installationUuid = "")

        givenAuthenticated()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(analyticsRequest)
            .post("/api/v1/analytics")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .body("status", Matchers.equalTo(400))
            .body("error", Matchers.equalTo("Bad Request"))
            .body("message", Matchers.equalTo("Request body contains invalid attributes"))
            .body("timestamp", isStringDateBetweenNowAndTenSecondsAgo())
            .body("path", Matchers.equalTo("/api/v1/analytics"))
            .body(
                "errors", Matchers.contains(
                    mapOf(
                        "field" to "installationUuid",
                        "code" to "Size",
                        "message" to "size must be between 1 and 64"
                    )
                )
            )

    }

    @Test
    fun bad_request_on_null_installationUuid() {
        val timestamp = OffsetDateTime.parse("2020-12-17T10:59:17.123Z")

        val analyticsJsonRequest = """
            {
                "installationUuid": null,
                "infos": {
                    "os": "Android",
                    "type": 0,
                    "load": 1.03,
                    "root": false
                },
                "timestamp": "$timestamp",
                "events": [
                    {
                        "name": "eventName1",
                        "timestamp": "$timestamp",
                        "desc": "event1 description"
                    },
                    {
                        "name": "eventName2",
                        "timestamp": "$timestamp"
                    }
                ],
                "errors": [
                    {
                        "name": "errorName1",
                        "timestamp": "$timestamp"
                    },
                    {
                        "name": "errorName2",
                        "timestamp": "$timestamp",
                        "desc": "error2 description"
                    }
                ]
            }
            """
        givenAuthenticated()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(analyticsJsonRequest)
            .post("/api/v1/analytics")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .body("status", Matchers.equalTo(400))
            .body("error", Matchers.equalTo("Bad Request"))
            .body("timestamp", isStringDateBetweenNowAndTenSecondsAgo())
            .body("path", Matchers.equalTo("/api/v1/analytics"))
            .body("errors", Matchers.nullValue())
    }

    @Test
    fun bad_request_on_empty_event_name() {
        val timestamp = OffsetDateTime.parse("2020-12-17T10:59:17.123Z")
        val analyticsRequest = ExampleData.analyticsRequest().copy(
            events = listOf(
                TimestampedEvent(
                    name = "",
                    timestamp = timestamp,
                    desc = "event1 description"
                ),
                TimestampedEvent(
                    name = "eventName2",
                    timestamp = timestamp
                )
            )
        )

        givenAuthenticated()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(analyticsRequest)
            .post("/api/v1/analytics")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .body("status", Matchers.equalTo(400))
            .body("error", Matchers.equalTo("Bad Request"))
            .body("message", Matchers.equalTo("Request body contains invalid attributes"))
            .body("timestamp", isStringDateBetweenNowAndTenSecondsAgo())
            .body("path", Matchers.equalTo("/api/v1/analytics"))
            .body(
                "errors", Matchers.contains(
                    mapOf(
                        "field" to "events[0].name",
                        "code" to "Size",
                        "message" to "size must be between 1 and ${Integer.MAX_VALUE}"
                    )
                )
            )

    }

    @Test
    fun bad_request_on_null_event_name() {
        val timestamp = OffsetDateTime.parse("2020-12-17T10:59:17.123Z")
        val analyticsJsonRequest = """
            {
                "installationUuid": "${UUID.randomUUID()}",
                "infos": {
                    "os": "Android",
                    "type": 0,
                    "load": 1.03,
                    "root": false
                },
                "timestamp": "$timestamp",
                "events": [
                    {
                        "name": null,
                        "timestamp": "$timestamp",
                        "desc": "event1 description"
                    },
                    {
                        "name": "eventName2",
                        "timestamp": "$timestamp"
                    }
                ],
                "errors": [
                    {
                        "name": "errorName1",
                        "timestamp": "$timestamp"
                    },
                    {
                        "name": "errorName2",
                        "timestamp": "$timestamp",
                        "desc": "error2 description"
                    }
                ]
            }
            """
        givenAuthenticated()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(analyticsJsonRequest)
            .post("/api/v1/analytics")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .body("status", Matchers.equalTo(400))
            .body("error", Matchers.equalTo("Bad Request"))
            .body("timestamp", isStringDateBetweenNowAndTenSecondsAgo())
            .body("path", Matchers.equalTo("/api/v1/analytics"))
            .body("errors", Matchers.nullValue())
    }

    @Test
    fun bad_request_on_malformed_event_timestamp(@Autowired objectMapper: ObjectMapper?) {
        val eventWithMalformedTimestamp = mapOf(
            "name" to "eventName",
            "timestamp" to "2021-06-02T22:47:02.388 PMZ"
        )
        givenAuthenticated()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(
                mapOf(
                    "installationUuid" to UUID.randomUUID(),
                    "events" to listOf(eventWithMalformedTimestamp),
                    "errors" to listOf<Map<String, Any>>()
                )
            )
            .post("/api/v1/analytics")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .body("status", Matchers.equalTo(400))
            .body("error", Matchers.equalTo("Bad Request"))
            .body(
                "message", Matchers.startsWith(
                    "JSON parse error: Cannot deserialize value of type `java.time.OffsetDateTime` from String \"2021-06-02T22:47:02.388 PMZ\""
                )
            )
            .body("timestamp", isStringDateBetweenNowAndTenSecondsAgo())
            .body("path", Matchers.equalTo("/api/v1/analytics"))
    }

    @Test
    fun bad_request_on_empty_error_name() {
        val timestamp = OffsetDateTime.parse("2020-12-17T10:59:17.123Z")
        val analyticsRequest = ExampleData.analyticsRequest().copy(
            errors = listOf(
                TimestampedEvent(
                    name = "",
                    timestamp = timestamp,
                    desc = "event1 description"
                ),
                TimestampedEvent(
                    name = "errorName2",
                    timestamp = timestamp
                )
            )
        )

        givenAuthenticated()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(analyticsRequest)
            .post("/api/v1/analytics")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .body("status", Matchers.equalTo(400))
            .body("error", Matchers.equalTo("Bad Request"))
            .body("message", Matchers.equalTo("Request body contains invalid attributes"))
            .body("timestamp", isStringDateBetweenNowAndTenSecondsAgo())
            .body("path", Matchers.equalTo("/api/v1/analytics"))
            .body(
                "errors", Matchers.contains(
                    mapOf(
                        "field" to "errors[0].name",
                        "code" to "Size",
                        "message" to "size must be between 1 and ${Integer.MAX_VALUE}"
                    )
                )
            )
    }

    @Test
    fun bad_request_on_null_error_name() {
        val timestamp = OffsetDateTime.parse("2020-12-17T10:59:17.123Z")
        val analyticsJsonRequest = """
            {
                "installationUuid": "${UUID.randomUUID()}",
                "infos": {
                    "os": "Android",
                    "type": 0,
                    "load": 1.03,
                    "root": false
                },
                "timestamp": "$timestamp",
                "events": [
                    {
                        "name": "eventName1",
                        "timestamp": "$timestamp",
                        "desc": "event1 description"
                    },
                    {
                        "name": "eventName2",
                        "timestamp": "$timestamp"
                    }
                ],
                "errors": [
                    {
                        "name": null,
                        "timestamp": "$timestamp"
                    },
                    {
                        "name": "errorName2",
                        "timestamp": "$timestamp",
                        "desc": "error2 description"
                    }
                ]
            }
            """

        givenAuthenticated()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(analyticsJsonRequest)
            .post("/api/v1/analytics")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .body("status", Matchers.equalTo(400))
            .body("error", Matchers.equalTo("Bad Request"))
            .body("timestamp", isStringDateBetweenNowAndTenSecondsAgo())
            .body("path", Matchers.equalTo("/api/v1/analytics"))
            .body("errors", Matchers.nullValue())
    }

    @Test
    fun bad_request_on_null_error_timestamp() {
        val timestamp = OffsetDateTime.parse("2020-12-17T10:59:17.123Z")

        val analyticsJsonRequest = """
            {
                "installationUuid": "${UUID.randomUUID()}",
                "infos": {
                    "os": "Android",
                    "type": 0,
                    "load": 1.03,
                    "root": false
                },
                "timestamp": "$timestamp",
                "events": [
                    {
                        "name": "eventName1",
                        "timestamp": "$timestamp",
                        "desc": "event1 description"
                    },
                    {
                        "name": "eventName2",
                        "timestamp": "$timestamp"
                    }
                ],
                "errors": [
                    {
                        "name": "errorName1",
                        "timestamp": null
                    },
                    {
                        "name": "errorName2",
                        "timestamp": "$timestamp",
                        "desc": "error2 description"
                    }
                ]
            }
            """

        givenAuthenticated()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(analyticsJsonRequest)
            .post("/api/v1/analytics")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .body("status", Matchers.equalTo(400))
            .body("error", Matchers.equalTo("Bad Request"))
            .body("timestamp", isStringDateBetweenNowAndTenSecondsAgo())
            .body("path", Matchers.equalTo("/api/v1/analytics"))
            .body("errors", Matchers.nullValue())
    }
}