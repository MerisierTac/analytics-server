package fr.gouv.tac.analytics.server.config;

import javax.validation.constraints.NotEmpty;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "analytics")
@Validated
@Data
public class AnalyticsProperties {

    @NotEmpty
    private String creationTopic;

    @NotEmpty
    private String deletionTopic;

    @NotEmpty
    private String robertJwtAnalyticsPublicKey;
}
