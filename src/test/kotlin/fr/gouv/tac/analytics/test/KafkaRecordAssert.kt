package fr.gouv.tac.analytics.test

import com.fasterxml.jackson.databind.JsonNode
import com.jayway.jsonpath.JsonPath
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.header.Header
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.HamcrestCondition
import org.hamcrest.Matcher
import org.hamcrest.core.IsEqual

class KafkaRecordAssert private constructor(private val actualRecord: ConsumerRecord<out Any?, JsonNode>) :
    AbstractAssert<KafkaRecordAssert?, ConsumerRecord<out Any?, JsonNode>?>(
        actualRecord,
        KafkaRecordAssert::class.java
    ) {

    fun <T> hasJsonValue(jsonPath: String, matcher: Matcher<T>): KafkaRecordAssert {
        val jsonValue = actualRecord.value()?.toPrettyString()
        val jsonPathValue: T = JsonPath.compile(jsonPath).read(jsonValue)
        assertThat(jsonPathValue).satisfies(HamcrestCondition(matcher))
        return this
    }

    fun <T> hasJsonValue(jsonPath: String, expectedValue: T): KafkaRecordAssert {
        return hasJsonValue(jsonPath, IsEqual(expectedValue))
    }

    fun hasNoHeader(headerName: String): KafkaRecordAssert {
        assertThat(actualRecord.headers().headers(headerName))
            .extracting<ByteArray, RuntimeException> { header: Header -> header.value() }
            .extracting<String, RuntimeException> { headerByteValue: ByteArray -> String(headerByteValue) }
            .`as`("Kafka record shouldn't have a '%s' header", headerName)
            .isEmpty()
        return this
    }

    fun hasNoKey(): KafkaRecordAssert {
        assertThat(actualRecord.key())
            .`as`("Kafka record shouldn't have a key")
            .isNull()
        return this
    }

    companion object {
        fun assertThat(consumerRecord: ConsumerRecord<out Any?, JsonNode>): KafkaRecordAssert {
            return KafkaRecordAssert(consumerRecord)
        }
    }
}
