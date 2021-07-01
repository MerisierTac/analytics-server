package fr.gouv.tac.analytics.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
class KafkaConfiguration (private val kafkaProperties: KafkaProperties, private val objectMapper: ObjectMapper){

    @Bean
    fun producerFactory(): ProducerFactory<String, Any> {
        val jsonSerializer = JsonSerializer<Any>(objectMapper)
        jsonSerializer.isAddTypeInfo = false
        return DefaultKafkaProducerFactory(
            kafkaProperties.buildConsumerProperties(),
            StringSerializer(),
            jsonSerializer
        )
    }

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, Any> = KafkaTemplate(producerFactory())
}
