package fr.gouv.tac.analytics.service

import fr.gouv.tac.analytics.model.AnalyticsCreation
import fr.gouv.tac.analytics.model.AnalyticsDeletion
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.util.concurrent.ListenableFutureCallback
import java.time.Instant

@Service
class AnalyticsService(
    private val creationKafkaTemplate: KafkaTemplate<String, AnalyticsCreation>,
    private val deletionKafkaTemplate: KafkaTemplate<String, AnalyticsDeletion>
) {
    private val log = LoggerFactory.getLogger(AnalyticsService::class.java)

    fun createAnalytics(analyticsCreation: AnalyticsCreation?) {
        creationKafkaTemplate.sendDefault(analyticsCreation).addCallback(object :
            ListenableFutureCallback<org.springframework.kafka.support.SendResult<String?, AnalyticsCreation?>?> {
            override fun onFailure(throwable: Throwable) {
                log.warn("Analytics creation - error sending message to kafka", throwable)
            }

            override fun onSuccess(sendResult: org.springframework.kafka.support.SendResult<String?, AnalyticsCreation?>?) {
                log.debug("Message successfully sent {}", sendResult)
            }
        })
    }

    fun deleteAnalytics(installationUuid: String) {
        val analyticsDeletion = AnalyticsDeletion(installationId = installationUuid, deletionTimestamp = Instant.now())
        deletionKafkaTemplate.sendDefault(analyticsDeletion).addCallback(object :
            ListenableFutureCallback<org.springframework.kafka.support.SendResult<String?, AnalyticsDeletion?>?> {
            override fun onFailure(throwable: Throwable) {
                log.warn("Analytics deletion - error sending message to kafka", throwable)
            }

            override fun onSuccess(sendResult: org.springframework.kafka.support.SendResult<String?, AnalyticsDeletion?>?) {
                log.debug("Message successfully sent {}", sendResult)
            }
        })
    }
}