package fr.gouv.tac.analytics.test

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import fr.gouv.tac.analytics.test.KafkaRecordAssert.Companion.assertThat
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets

class KafkaRecordAssertTest {
    private var exampleConsumerRecord: ConsumerRecord<String, JsonNode>? = null

    @BeforeEach
    fun setup() {
        val jsonValue = ObjectMapper().readTree("{\"name\": \"Robert\"}")
        exampleConsumerRecord = ConsumerRecord("topic-name", 0, 0, "key", jsonValue)
        exampleConsumerRecord!!.headers().add("TestHeader", "HeaderValue".toByteArray(StandardCharsets.UTF_8))
    }

    @Test
    fun can_detect_hasNoKey_mismatch() {
        assertThatThrownBy { exampleConsumerRecord?.let { assertThat(it).hasNoKey() } }
            .hasMessageFindingMatch(
                """
                        \[Kafka record shouldn't have a key\] .*
                        expected: null.*
                        but was : "key".*
                """.trimIndent()
            )
    }

    @Test
    fun can_detect_hasNoHeader_mismatch() {
        assertThatThrownBy { exampleConsumerRecord?.let { assertThat(it).hasNoHeader("TestHeader") } }
            .hasMessageFindingMatch(
                """
                        \[Kafka record shouldn't have a 'TestHeader' header\] .*
                        Expecting empty but was: \["HeaderValue"\]
                """.trimIndent()
            )
    }

    @Test
    fun can_detect_hasJsonValue_mismatch() {
        assertThatThrownBy { exampleConsumerRecord?.let { assertThat(it).hasJsonValue("name", "clea") } }
            .hasMessageFindingMatch(
                """
             Expecting:.*
               "Robert".*
             to satisfy:.*
               "clea"
                """.trimIndent()
            )
    }

    @Test
    fun can_detect_hasJsonValue_mismatch_with_matcher() {
        assertThatThrownBy {
            exampleConsumerRecord?.let {
                assertThat(it).hasJsonValue(
                    "name",
                    Matchers.equalTo("clea")
                )
            }
        }.hasMessageFindingMatch(
            """
             Expecting:.*
               "Robert".*
             to satisfy:.*
               "clea"
            """.trimIndent()
        )
    }
}
