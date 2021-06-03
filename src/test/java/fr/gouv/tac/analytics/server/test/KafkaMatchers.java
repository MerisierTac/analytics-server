package fr.gouv.tac.analytics.server.test;

import fr.gouv.tac.analytics.server.service.kafka.model.AnalyticsDeletion;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

public class KafkaMatchers {

    public static KafkaDeleteAnalyticsMessageCondition recentAnalyticsDeleteMessageWithInstallationUuid(UUID installationUuid) {
        return new KafkaDeleteAnalyticsMessageCondition(installationUuid);
    }

    public static KafkaDeleteAnalyticsMessageCondition recentAnalyticsDeleteMessageWithInstallationUuid(String installationUuid) {
        return new KafkaDeleteAnalyticsMessageCondition(UUID.fromString(installationUuid));
    }

    public static class KafkaDeleteAnalyticsMessageCondition extends Condition<AnalyticsDeletion> {

        private final Instant minTime =  Instant.now().minus(5, SECONDS);
        private final Instant maxTime =  Instant.now();
        private final UUID expectedInstallationUuid;

        public KafkaDeleteAnalyticsMessageCondition(UUID expectedInstallationUuid) {
            as("an AnalyticsDeletion with 'installationUuid' %s and 'timestamp' %s < t < %s", expectedInstallationUuid, minTime, maxTime);
            this.expectedInstallationUuid = expectedInstallationUuid;
        }

        @Override
        public boolean matches(AnalyticsDeletion value) {
            if (value == null) {
                return false;
            }
            return expectedInstallationUuid.equals(value.getInstallationUuid())
                    && value.getTimestamp().isAfter(minTime)
                    && value.getTimestamp().isBefore(maxTime);
        }
    }
}
