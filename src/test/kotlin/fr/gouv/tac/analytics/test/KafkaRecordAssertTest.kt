package fr.gouv.tac.analytics.test

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import fr.gouv.tac.analytics.test.KafkaRecordAssert.Companion.assertThat
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.assertj.core.api.Assertions
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.IOException
import java.nio.charset.StandardCharsets

class KafkaRecordAssertTest {
    private var exampleConsumerRecord: ConsumerRecord<String, JsonNode>? = null

    @BeforeEach
    @Throws(IOException::class)
    fun setup() {
        val jsonValue = ObjectMapper().readTree("{\"name\": \"Robert\"}")
        exampleConsumerRecord = ConsumerRecord("topic-name", 0, 0, "key", jsonValue)
        exampleConsumerRecord!!.headers().add("TestHeader", "HeaderValue".toByteArray(StandardCharsets.UTF_8))
    }

    @Test
    fun can_detect_hasNoKey_mismatch() {
        Assertions.assertThatThrownBy { exampleConsumerRecord?.let { assertThat(it).hasNoKey() } }
            .hasMessage(
                "[Kafka record shouldn't have a key] \n" +
                    "Expecting:\n <\"key\">\n" +
                    "to be equal to:\n <null>\n" +
                    "but was not."
            )
    }

    @Test
    fun can_detect_hasNoHeader_mismatch() {
        Assertions.assertThatThrownBy { exampleConsumerRecord?.let { assertThat(it).hasNoHeader("TestHeader") } }
            .hasMessage(
                "[Kafka record shouldn't have a 'TestHeader' header] \n" +
                    "Expecting empty but was:<[\"HeaderValue\"]>"
            )
    }

    @Test
    fun can_detect_hasJsonValue_mismatch() {
        Assertions.assertThatThrownBy { exampleConsumerRecord?.let { assertThat(it).hasJsonValue("name", "clea") } }
            .hasMessage(
                "\nExpecting:\n  <\"Robert\">\n" +
                    "to satisfy:\n  <\"clea\">"
            )
    }

    @Test
    fun can_detect_hasJsonValue_mismatch_with_matcher() {
        Assertions.assertThatThrownBy {
            exampleConsumerRecord?.let {
                assertThat(it).hasJsonValue(
                    "name",
                    Matchers.equalTo("clea")
                )
            }
        }
            .hasMessage(
                (
                    "\nExpecting:\n  <\"Robert\">\n" +
                        "to satisfy:\n  <\"clea\">"
                    )
            )
    }
}
