package fr.gouv.tac.analytics.model

import lombok.With
import java.time.Instant
import java.time.OffsetDateTime

data class AnalyticsCreation(
    val installationUuid: String,
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
    val installationUuid: String,
    val deletionTimestamp: Instant
)