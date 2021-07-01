package fr.gouv.tac.analytics.service

import fr.gouv.tac.analytics.config.AnalyticsProperties
import fr.gouv.tac.analytics.model.AnalyticsCreation
import fr.gouv.tac.analytics.model.AnalyticsDeletion
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.time.Instant.now
import java.time.OffsetDateTime

@Service
class AnalyticsService(
    private val analyticsProperties: AnalyticsProperties,
    private val kafkaTemplate: KafkaTemplate<String, Any>
) {
    private val log = LoggerFactory.getLogger(AnalyticsService::class.java)

    fun createAnalytics(analytics: AnalyticsCreation) {
        val analyticsEvent = analytics.copy(creationDate = OffsetDateTime.now())
        kafkaTemplate.send(analyticsProperties.creationTopic, analyticsEvent)
            .addCallback(
                { log.debug("Message successfully sent {}", it) },
                { log.warn("Analytics creation - error sending message to kafka", it) }
            )
    }

    fun deleteAnalytics(installationUuid: String) {
        val analyticsDeletion = AnalyticsDeletion(
            installationUuid = installationUuid,
            deletionTimestamp = now()
        )
        kafkaTemplate.send(analyticsProperties.deletionTopic, analyticsDeletion)
            .addCallback(
                { log.debug("Message successfully sent {}", it) },
                { log.warn("Analytics deletion - error sending message to kafka", it) }
            )
    }
}