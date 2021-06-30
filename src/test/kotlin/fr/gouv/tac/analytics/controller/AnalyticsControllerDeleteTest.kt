package fr.gouv.tac.analytics.controller

import com.fasterxml.jackson.databind.JsonNode
import fr.gouv.tac.analytics.test.IntegrationTest
import fr.gouv.tac.analytics.test.KafkaManager
import fr.gouv.tac.analytics.test.KafkaRecordAssert
import fr.gouv.tac.analytics.test.RestAssuredManager.Companion.givenAuthenticated
import fr.gouv.tac.analytics.test.TemporalMatchers.isStringDateBetweenNowAndTenSecondsAgo
import io.restassured.http.ContentType
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.HamcrestCondition
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.util.*
import java.util.function.Consumer

@IntegrationTest
internal class AnalyticsControllerDeleteTest {
    @Test
    fun can_send_a_delete_message_in_kafka() {
        val installationUuid = UUID.randomUUID()
        givenAuthenticated()
            .delete("/api/v1/analytics?installationUuid={uuid}", installationUuid)
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value())
            .body(Matchers.emptyString())
        KafkaRecordAssert.assertThat(KafkaManager.getSingleRecord("dev.analytics.cmd.delete"))
            .hasNoHeader("__TypeId__")
            .hasNoKey()
            .hasJsonValue("installationUuid", installationUuid.toString())
            .hasJsonValue("deletionTimestamp", isStringDateBetweenNowAndTenSecondsAgo())
    }

    @Test
    fun can_send_multiple_delete_messages_in_kafka() {
        val uuids = listOf(
            "8ef146ad-56c1-488a-af2a-99fd5b76b7bd",
            "21d76e63-7ab4-480e-9ba5-330ee63d38cf",
            "23498429-3c79-42a8-ac99-bc4c1742c2bd",
            "ca5739b5-3aad-4f7f-bcb2-5c5b99ffc2a9",
            "c33a4891-6d43-4fe3-95b6-d28a6bf30deb"
        )
        uuids.forEach(Consumer { installationUuid: String? ->
            givenAuthenticated()
                .delete("/api/v1/analytics?installationUuid={uuid}", installationUuid)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value())
                .body(Matchers.emptyString())
        })
        val kafkaRecords = KafkaManager.records.records("dev.analytics.cmd.delete")
        assertThat(kafkaRecords)
            .`as`("each input installationUuid should be in a record")
            .extracting<String> { record: ConsumerRecord<String, JsonNode> -> record.value().get("installationUuid").textValue() }
            .containsExactlyElementsOf(uuids)
        assertThat(kafkaRecords)
            .`as`("all records should have a recent timestamp")
            .extracting<String> { record: ConsumerRecord<String, JsonNode> -> record.value().get("deletionTimestamp").textValue() }
            .are(HamcrestCondition(isStringDateBetweenNowAndTenSecondsAgo()))
    }

    @Test
    fun bad_request_on_missing_param_installationUuid() {
        givenAuthenticated()
            .delete("/api/v1/analytics")
            .then()
            .contentType(ContentType.JSON)
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .body("status", Matchers.equalTo(400))
            .body("error", Matchers.equalTo("Bad Request"))
            .body("message", Matchers.equalTo("Required String parameter 'installationUuid' is not present"))
            .body("timestamp", isStringDateBetweenNowAndTenSecondsAgo())
            .body("path", Matchers.equalTo("/api/v1/analytics"))
    }

    @Test
    fun bad_request_on_empty_param_installationUuid() {
        givenAuthenticated()
            .delete("/api/v1/analytics?installationUuid=")
            .then()
            .contentType(ContentType.JSON)
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .body("status", Matchers.equalTo(400))
            .body("error", Matchers.equalTo("Bad Request"))
            .body("message", Matchers.equalTo("Request body contains invalid attributes"))
            .body("timestamp", isStringDateBetweenNowAndTenSecondsAgo())
            .body("path", Matchers.equalTo("/api/v1/analytics"))
            .body(
                "errors", Matchers.contains(
                    mapOf(
                        "field" to "deleteAnalytics.installationUuid",
                        "code" to "Size",
                        "message" to "size must be between 1 and 64"
                    )
                )
            )
    }

    @Test
    fun bad_request_on_tooLong_param_installationUuid() {
        val installationUuid = UUID.randomUUID()
        givenAuthenticated()
            .delete(
                "/api/v1/analytics?installationUuid={uuid}",
                installationUuid.toString() + installationUuid.toString()
            )
            .then()
            .contentType(ContentType.JSON)
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .body("status", Matchers.equalTo(400))
            .body("error", Matchers.equalTo("Bad Request"))
            .body("message", Matchers.equalTo("Request body contains invalid attributes"))
            .body("timestamp", isStringDateBetweenNowAndTenSecondsAgo())
            .body("path", Matchers.equalTo("/api/v1/analytics"))
            .body(
                "errors", Matchers.contains(
                    mapOf(
                        "field" to "deleteAnalytics.installationUuid",
                        "code" to "Size",
                        "message" to "size must be between 1 and 64"
                    )
                )
            )
    }
}