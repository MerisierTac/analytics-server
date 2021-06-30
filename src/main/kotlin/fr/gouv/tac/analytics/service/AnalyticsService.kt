package fr.gouv.tac.analytics.service

import fr.gouv.tac.analytics.config.AnalyticsProperties
import fr.gouv.tac.analytics.model.AnalyticsCreation
import fr.gouv.tac.analytics.model.AnalyticsDeletion
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Service
import java.time.Instant
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
                { sendResult: SendResult<String, Any>? ->
                    log.debug(
                        "Message successfully sent {}",
                        sendResult
                    )
                }
            ) { throwable: Throwable? ->
                log.warn(
                    "Analytics creation - error sending message to kafka",
                    throwable
                )
            }
    }

    fun deleteAnalytics(installationUuid: String) {
        val analyticsDeletion = AnalyticsDeletion(
            installationUuid = installationUuid,
            deletionTimestamp = Instant.now()
        )
        kafkaTemplate.send(analyticsProperties.deletionTopic, analyticsDeletion)
            .addCallback(
                { sendResult: SendResult<String, Any>? ->
                    log.debug(
                        "Message successfully sent {}",
                        sendResult
                    )
                }
            ) { throwable: Throwable? ->
                log.warn(
                    "Analytics deletion - error sending message to kafka",
                    throwable
                )
            }
    }
}