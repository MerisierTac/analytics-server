package fr.gouv.tac.analytics.test

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import fr.gouv.tac.analytics.test.KafkaRecordAssert.Companion.assertThat
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets

class KafkaRecordAssertTest {
    private val exampleConsumerRecord: ConsumerRecord<String, JsonNode>

    init {
        val jsonValue = ObjectMapper().readTree(
            """
            {
              "name": "Robert"
            }
            """.trimIndent()
        )
        exampleConsumerRecord = ConsumerRecord("topic-name", 0, 0, "key", jsonValue)
        exampleConsumerRecord.headers().add("TestHeader", "HeaderValue".toByteArray(StandardCharsets.UTF_8))
    }

    @Test
    fun can_detect_hasNoKey_mismatch() {
        assertThatThrownBy { assertThat(exampleConsumerRecord).hasNoKey() }
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
        assertThatThrownBy { assertThat(exampleConsumerRecord).hasNoHeader("TestHeader") }
            .hasMessageFindingMatch(
                """
                        \[Kafka record shouldn't have a 'TestHeader' header\] .*
                        Expecting empty but was: \["HeaderValue"\]
                """.trimIndent()
            )
    }

    @Test
    fun can_detect_hasJsonValue_mismatch() {
        assertThatThrownBy { assertThat(exampleConsumerRecord).hasJsonValue("name", "clea") }
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
            assertThat(exampleConsumerRecord).hasJsonValue(
                "name",
                Matchers.equalTo("clea")
            )
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
