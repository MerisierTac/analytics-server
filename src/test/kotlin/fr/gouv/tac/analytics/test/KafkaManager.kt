package fr.gouv.tac.analytics.test

import com.fasterxml.jackson.databind.JsonNode
import fr.gouv.tac.analytics.config.AnalyticsProperties
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.context.TestContext
import org.springframework.test.context.TestExecutionListener
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName

/**
 * A [TestExecutionListener] to start a Kafka container to be used as a dependency for SpringBootTests.
 *
 *
 * It starts a Karfka container statically and export required system properties to override Spring application context
 * configuration.
 *
 *
 * It starts / closes a consumer before and after each test method.
 *
 *
 * Static method [KafkaManager.getSingleRecord] can be used to fetch messages from Kafka.
 */
class KafkaManager : TestExecutionListener {
    companion object {
        private val KAFKA = KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:5.4.4")
        )
        private var consumer: Consumer<String, JsonNode>? = null

        fun getRecords(): ConsumerRecords<String, JsonNode> = KafkaTestUtils.getRecords(consumer)

        fun getSingleRecord(topic: String?): ConsumerRecord<String, JsonNode> {
            return KafkaTestUtils.getSingleRecord(consumer, topic)
        }

        init {
            KAFKA.start()
            System.setProperty("spring.kafka.bootstrap-servers", KAFKA.bootstrapServers)
        }
    }

    override fun beforeTestMethod(testContext: TestContext) {
        val analyticsProperties = testContext.applicationContext.getBean(AnalyticsProperties::class.java)
        val topics = listOf(
            analyticsProperties.creationTopic,
            analyticsProperties.deletionTopic
        )
        val config = KafkaTestUtils.consumerProps(KAFKA.bootstrapServers, "test-consumer", "false")
        consumer = DefaultKafkaConsumerFactory(config, StringDeserializer(), JsonDeserializer(JsonNode::class.java))
            .createConsumer()
        consumer!!.subscribe(topics)
    }

    override fun afterTestMethod(testContext: TestContext) {
        consumer!!.commitSync()
        consumer!!.close()
    }
}
