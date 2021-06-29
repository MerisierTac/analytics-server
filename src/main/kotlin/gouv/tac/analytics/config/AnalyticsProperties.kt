package fr.gouv.tac.analytics.config

import lombok.Data
import lombok.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated

@Component
@ConfigurationProperties(prefix = "analytics")
@Validated
@ConstructorBinding
@Value
class AnalyticsProperties(val creationTopic: String = "", val deletionTopic: String = "", val robertJwtAnalyticsPublicKey: String = "")