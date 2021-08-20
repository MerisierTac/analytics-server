package fr.gouv.tac.analytics.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.validation.annotation.Validated

@Validated
@ConstructorBinding
@ConfigurationProperties(prefix = "analytics")
class AnalyticsProperties(
    val creationTopic: String,
    val deletionTopic: String,
    val robertJwtAnalyticsPublicKey: String
)
