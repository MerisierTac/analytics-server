package fr.gouv.tac.analytics.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated

@Component
@ConfigurationProperties(prefix = "analytics")
@Validated
class AnalyticsProperties(
    var creationTopic: String = "",
    var deletionTopic: String = "",
    var robertJwtAnalyticsPublicKey: String = ""
)
