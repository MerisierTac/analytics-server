package fr.gouv.tac.analytics.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@Component
@ConfigurationProperties(prefix = "analytics")
@Validated
@Data
public class AnalyticsProperties {

    @NotEmpty
    private String creationTopic;

    @NotEmpty
    private String deletionTopic;
}
