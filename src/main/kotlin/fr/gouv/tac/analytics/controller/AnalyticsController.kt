package fr.gouv.tac.analytics.controller

import fr.gouv.tac.analytics.api.AnalyticsApi
import fr.gouv.tac.analytics.api.model.AnalyticsRequest
import fr.gouv.tac.analytics.api.model.TimestampedEvent
import fr.gouv.tac.analytics.model.AnalyticsCreation
import fr.gouv.tac.analytics.model.AnalyticsEvent
import fr.gouv.tac.analytics.service.AnalyticsService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping(path = ["/api/v1"])
class AnalyticsController(private val analyticsService: AnalyticsService) : AnalyticsApi {

    private val log = LoggerFactory.getLogger(AnalyticsController::class.java)

    override fun createAnalytics(analyticsRequest: AnalyticsRequest?): ResponseEntity<Unit> {
        analyticsService.createAnalytics(
            AnalyticsCreation(
                installationUuid = analyticsRequest!!.installationUuid,
                infos = analyticsRequest.infos,
                events = analyticsRequest.events.map(toAnalyticsEvent()),
                errors = analyticsRequest.errors.map(toAnalyticsEvent())
            )
        )
        return ResponseEntity.ok().build()
    }

    private fun toAnalyticsEvent() = { t: TimestampedEvent -> AnalyticsEvent(t.name, t.timestamp, t.desc) }

    override fun deleteAnalytics(installationUuid: String): ResponseEntity<Unit> {
        log.info("Analytics deletion order has been received from mobile application : {}", installationUuid)
        analyticsService.deleteAnalytics(installationUuid)
        return ResponseEntity.noContent().build()
    }
}