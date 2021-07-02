package fr.gouv.tac.analytics.test

import fr.gouv.tac.analytics.api.model.AnalyticsRequest
import fr.gouv.tac.analytics.api.model.TimestampedEvent
import java.time.OffsetDateTime
import java.util.UUID

object ExampleData {
    fun analyticsRequest(): AnalyticsRequest {
        val infos = mapOf(
            "os" to "Android",
            "type" to 0,
            "load" to 1.03,
            "root" to false
        )
        val timestamp = OffsetDateTime.parse("2020-12-17T10:59:17.123Z")

        return AnalyticsRequest(
            installationUuid = UUID.randomUUID().toString(),
            infos = infos,
            events = listOf(
                TimestampedEvent(
                    name = "eventName1",
                    timestamp = timestamp,
                    desc = "event1 description"
                ),
                TimestampedEvent(
                    name = "eventName2",
                    timestamp = timestamp
                )
            ),
            errors = listOf(
                TimestampedEvent(
                    name = "errorName1",
                    timestamp = timestamp
                ),
                TimestampedEvent(
                    name = "errorName2",
                    timestamp = timestamp,
                    desc = "error2 description"
                )
            )
        )
    }
}
