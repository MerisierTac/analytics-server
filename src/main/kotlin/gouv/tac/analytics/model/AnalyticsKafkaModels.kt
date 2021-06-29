package fr.gouv.tac.analytics.model

import java.time.Instant
import java.time.OffsetDateTime

data class AnalyticsCreation(
    val installationId: String,
    val infos: Map<String, Any>?,
    val events: List<AnalyticsEvent>?,
    val errors: List<AnalyticsEvent>?,
    val creationDate: OffsetDateTime = OffsetDateTime.now()
)

data class AnalyticsEvent(
    val name: String,
    val timestamp: OffsetDateTime,
    val desc: String?
)

data class AnalyticsDeletion(
    val installationId: String,
    val deletionTimestamp: Instant
)