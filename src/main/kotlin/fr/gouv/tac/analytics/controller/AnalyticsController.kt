package fr.gouv.tac.analytics.controller

import fr.gouv.tac.analytics.api.AnalyticsApi
import fr.gouv.tac.analytics.api.model.AnalyticsRequest
import fr.gouv.tac.analytics.model.AnalyticsCreation
import fr.gouv.tac.analytics.model.AnalyticsEvent
import fr.gouv.tac.analytics.service.AnalyticsService
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping(path = ["/api/v1"])
class AnalyticsController(private val analyticsService: AnalyticsService) : AnalyticsApi {

    override fun createAnalytics(analyticsRequest: AnalyticsRequest?): ResponseEntity<Unit> {
        analyticsService.createAnalytics(
            AnalyticsCreation(
                installationUuid = analyticsRequest!!.installationUuid,
                infos = analyticsRequest.infos,
                events = analyticsRequest.events.map { AnalyticsEvent(it.name, it.timestamp, it.desc) },
                errors = analyticsRequest.errors.map { AnalyticsEvent(it.name, it.timestamp, it.desc) }
            )
        )
        return ResponseEntity.ok().build()
    }

    override fun deleteAnalytics(installationUuid: String): ResponseEntity<Unit> {
        analyticsService.deleteAnalytics(installationUuid)
        return ResponseEntity.noContent().build()
    }
}
