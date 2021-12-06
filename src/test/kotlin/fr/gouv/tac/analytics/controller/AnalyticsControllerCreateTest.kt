package fr.gouv.tac.analytics.controller

import com.fasterxml.jackson.databind.ObjectMapper
import fr.gouv.tac.analytics.api.model.TimestampedEvent
import fr.gouv.tac.analytics.test.ExampleData
import fr.gouv.tac.analytics.test.IntegrationTest
import fr.gouv.tac.analytics.test.KafkaManager
import fr.gouv.tac.analytics.test.KafkaRecordAssert.Companion.assertThat
import fr.gouv.tac.analytics.test.LogbackManager.Companion.assertThatInfoLogs
import fr.gouv.tac.analytics.test.RestAssuredManager.Companion.givenAuthenticated
import fr.gouv.tac.analytics.test.TemporalMatchers.isStringDateBetweenNowAndTenSecondsAgo
import io.restassured.http.ContentType.JSON
import org.assertj.core.api.HamcrestCondition
import org.hamcrest.Matchers.anEmptyMap
import org.hamcrest.Matchers.emptyString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.OK
import java.time.OffsetDateTime
import java.util.UUID

@IntegrationTest
class AnalyticsControllerCreateTest {

    @Test
    fun should_send_a_create_message_in_kafka() {
        val analyticsRequest = ExampleData.analyticsRequest()
        givenAuthenticated()
            .contentType(JSON)
            .body(analyticsRequest)
            .post("/api/v1/analytics")
            .then()
            .statusCode(OK.value())
            .body(emptyString())
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
    fun should_accept_null_infos_events_and_error_list() {
        val analyticsRequest = ExampleData.analyticsRequest()
            .copy(infos = null, events = null, errors = null)
        givenAuthenticated()
            .contentType(JSON)
            .body(analyticsRequest)
            .post("/api/v1/analytics")
            .then()
            .statusCode(OK.value())
            .body(emptyString())
        assertThat(KafkaManager.getSingleRecord("dev.analytics.cmd.create"))
            .hasNoHeader("__TypeId__")
            .hasNoKey()
            .hasJsonValue("creationDate", isStringDateBetweenNowAndTenSecondsAgo())
            .hasJsonValue("infos", anEmptyMap<String, String>())
            .hasJsonValue("events", hasSize<TimestampedEvent>(0))
            .hasJsonValue("errors", hasSize<TimestampedEvent>(0))
    }

    @Test
    fun bad_request_on_empty_installationUuid() {
        val analyticsRequest = ExampleData.analyticsRequest().copy(installationUuid = "")

        givenAuthenticated()
            .contentType(JSON)
            .body(analyticsRequest)
            .post("/api/v1/analytics")
            .then()
            .statusCode(BAD_REQUEST.value())
            .body("status", equalTo(400))
            .body("error", equalTo("Bad Request"))
            .body("message", equalTo("Request body contains invalid attributes"))
            .body("timestamp", isStringDateBetweenNowAndTenSecondsAgo())
            .body("path", equalTo("/api/v1/analytics"))
            .body("errors[0].field", equalTo("installationUuid"))
            .body("errors[0].code", equalTo("Size"))
            .body("errors[0].message", equalTo("size must be between 1 and 64"))

        assertThatInfoLogs()
            .contains("Validation error on POST /api/v1/analytics: 'installationUuid' size must be between 1 and 64")
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
            .contentType(JSON)
            .body(analyticsJsonRequest)
            .post("/api/v1/analytics")
            .then()
            .statusCode(BAD_REQUEST.value())
            .body("status", equalTo(400))
            .body("error", equalTo("Bad Request"))
            .body("message", equalTo("Request body contains invalid attributes"))
            .body("timestamp", isStringDateBetweenNowAndTenSecondsAgo())
            .body("path", equalTo("/api/v1/analytics"))
            .body("errors[0].field", equalTo("installationUuid"))
            .body("errors[0].code", equalTo("HttpMessageNotReadable"))
            .body("errors[0].message", startsWith("Instantiation of [simple type, class fr.gouv.tac.analytics.api.model.AnalyticsRequest] value failed for JSON property installationUuid due to missing (therefore NULL) value for creator parameter installationUuid which is a non-nullable type"))

        assertThatInfoLogs()
            .areExactly(1, HamcrestCondition(startsWith("Validation error on POST /api/v1/analytics: 'installationUuid' Instantiation of [simple type, class fr.gouv.tac.analytics.api.model.AnalyticsRequest] value failed for JSON property installationUuid due to missing (therefore NULL) value for creator parameter installationUuid which is a non-nullable type")))
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
            .contentType(JSON)
            .body(analyticsRequest)
            .post("/api/v1/analytics")
            .then()
            .statusCode(BAD_REQUEST.value())
            .body("status", equalTo(400))
            .body("error", equalTo("Bad Request"))
            .body("message", equalTo("Request body contains invalid attributes"))
            .body("timestamp", isStringDateBetweenNowAndTenSecondsAgo())
            .body("path", equalTo("/api/v1/analytics"))
            .body("errors[0].field", equalTo("events[0].name"))
            .body("errors[0].code", equalTo("Size"))
            .body("errors[0].message", startsWith("size must be between 1 and ${Integer.MAX_VALUE}"))

        assertThatInfoLogs()
            .areExactly(1, HamcrestCondition(startsWith("Validation error on POST /api/v1/analytics: 'events[0].name' size must be between 1 and ${Integer.MAX_VALUE}")))
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
            .contentType(JSON)
            .body(analyticsJsonRequest)
            .post("/api/v1/analytics")
            .then()
            .statusCode(BAD_REQUEST.value())
            .body("status", equalTo(400))
            .body("error", equalTo("Bad Request"))
            .body("message", equalTo("Request body contains invalid attributes"))
            .body("timestamp", isStringDateBetweenNowAndTenSecondsAgo())
            .body("path", equalTo("/api/v1/analytics"))
            .body("errors[0].field", equalTo("events[0].name"))
            .body("errors[0].code", equalTo("HttpMessageNotReadable"))
            .body("errors[0].message", startsWith("Instantiation of [simple type, class fr.gouv.tac.analytics.api.model.TimestampedEvent] value failed for JSON property name due to missing (therefore NULL) value for creator parameter name which is a non-nullable type"))

        assertThatInfoLogs()
            .areExactly(1, HamcrestCondition(startsWith("Validation error on POST /api/v1/analytics: 'events[0].name' Instantiation of [simple type, class fr.gouv.tac.analytics.api.model.TimestampedEvent] value failed for JSON property name due to missing (therefore NULL) value for creator parameter name which is a non-nullable type")))
    }

    @Test
    fun bad_request_on_malformed_event_timestamp(@Autowired objectMapper: ObjectMapper?) {
        val eventWithMalformedTimestamp = mapOf(
            "name" to "eventName",
            "timestamp" to "2021-06-02T22:47:02.388 PMZ"
        )
        givenAuthenticated()
            .contentType(JSON)
            .body(
                mapOf(
                    "installationUuid" to UUID.randomUUID(),
                    "events" to listOf(eventWithMalformedTimestamp),
                    "errors" to listOf<Map<String, Any>>()
                )
            )
            .post("/api/v1/analytics")
            .then()
            .statusCode(BAD_REQUEST.value())
            .body("status", equalTo(400))
            .body("error", equalTo("Bad Request"))
            .body("message", equalTo("Request body contains invalid attributes"))
            .body("timestamp", isStringDateBetweenNowAndTenSecondsAgo())
            .body("path", equalTo("/api/v1/analytics"))
            .body("errors[0].field", equalTo("events[0].timestamp"))
            .body("errors[0].code", equalTo("HttpMessageNotReadable"))
            .body("errors[0].message", equalTo("Text '2021-06-02T22:47:02.388 PMZ' could not be parsed at index 23"))

        assertThatInfoLogs()
            .areExactly(1, HamcrestCondition(startsWith("Validation error on POST /api/v1/analytics: 'events[0].timestamp' Text '2021-06-02T22:47:02.388 PMZ' could not be parsed at index 23")))
    }

    @Test
    fun bad_request_on_null_event_timestamp() {
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
                        "timestamp": null,
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
            .contentType(JSON)
            .body(analyticsJsonRequest)
            .post("/api/v1/analytics")
            .then()
            .statusCode(BAD_REQUEST.value())
            .body("status", equalTo(400))
            .body("error", equalTo("Bad Request"))
            .body("message", equalTo("Request body contains invalid attributes"))
            .body("timestamp", isStringDateBetweenNowAndTenSecondsAgo())
            .body("path", equalTo("/api/v1/analytics"))
            .body("errors[0].field", equalTo("events[0].timestamp"))
            .body("errors[0].code", equalTo("HttpMessageNotReadable"))
            .body("errors[0].message", startsWith("Instantiation of [simple type, class fr.gouv.tac.analytics.api.model.TimestampedEvent] value failed for JSON property timestamp due to missing (therefore NULL) value for creator parameter timestamp which is a non-nullable type"))

        assertThatInfoLogs()
            .areExactly(1, HamcrestCondition(startsWith("Validation error on POST /api/v1/analytics: 'events[0].timestamp' Instantiation of [simple type, class fr.gouv.tac.analytics.api.model.TimestampedEvent] value failed for JSON property timestamp due to missing (therefore NULL) value for creator parameter timestamp which is a non-nullable type")))
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
            .contentType(JSON)
            .body(analyticsRequest)
            .post("/api/v1/analytics")
            .then()
            .statusCode(BAD_REQUEST.value())
            .body("status", equalTo(400))
            .body("error", equalTo("Bad Request"))
            .body("message", equalTo("Request body contains invalid attributes"))
            .body("timestamp", isStringDateBetweenNowAndTenSecondsAgo())
            .body("path", equalTo("/api/v1/analytics"))
            .body("errors[0].field", equalTo("errors[0].name"))
            .body("errors[0].code", equalTo("Size"))
            .body("errors[0].message", equalTo("size must be between 1 and ${Integer.MAX_VALUE}"))

        assertThatInfoLogs()
            .areExactly(1, HamcrestCondition(startsWith("Validation error on POST /api/v1/analytics: 'errors[0].name' size must be between 1 and ${Integer.MAX_VALUE}")))
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
            .contentType(JSON)
            .body(analyticsJsonRequest)
            .post("/api/v1/analytics")
            .then()
            .statusCode(BAD_REQUEST.value())
            .body("status", equalTo(400))
            .body("error", equalTo("Bad Request"))
            .body("message", equalTo("Request body contains invalid attributes"))
            .body("timestamp", isStringDateBetweenNowAndTenSecondsAgo())
            .body("path", equalTo("/api/v1/analytics"))
            .body("errors[0].field", equalTo("errors[0].name"))
            .body("errors[0].code", equalTo("HttpMessageNotReadable"))
            .body("errors[0].message", startsWith("Instantiation of [simple type, class fr.gouv.tac.analytics.api.model.TimestampedEvent] value failed for JSON property name due to missing (therefore NULL) value for creator parameter name which is a non-nullable type"))

        assertThatInfoLogs()
            .areExactly(1, HamcrestCondition(startsWith("Validation error on POST /api/v1/analytics: 'errors[0].name' Instantiation of [simple type, class fr.gouv.tac.analytics.api.model.TimestampedEvent] value failed for JSON property name due to missing (therefore NULL) value for creator parameter name which is a non-nullable type")))
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
            .contentType(JSON)
            .body(analyticsJsonRequest)
            .post("/api/v1/analytics")
            .then()
            .statusCode(BAD_REQUEST.value())
            .body("status", equalTo(400))
            .body("error", equalTo("Bad Request"))
            .body("message", equalTo("Request body contains invalid attributes"))
            .body("timestamp", isStringDateBetweenNowAndTenSecondsAgo())
            .body("path", equalTo("/api/v1/analytics"))
            .body("errors[0].field", equalTo("errors[0].timestamp"))
            .body("errors[0].code", equalTo("HttpMessageNotReadable"))
            .body("errors[0].message", startsWith("Instantiation of [simple type, class fr.gouv.tac.analytics.api.model.TimestampedEvent] value failed for JSON property timestamp due to missing (therefore NULL) value for creator parameter timestamp which is a non-nullable type"))

        assertThatInfoLogs()
            .areExactly(1, HamcrestCondition(startsWith("Validation error on POST /api/v1/analytics: 'errors[0].timestamp' Instantiation of [simple type, class fr.gouv.tac.analytics.api.model.TimestampedEvent] value failed for JSON property timestamp due to missing (therefore NULL) value for creator parameter timestamp which is a non-nullable type")))
    }
}
