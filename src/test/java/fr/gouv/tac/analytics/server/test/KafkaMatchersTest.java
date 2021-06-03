package fr.gouv.tac.analytics.server.test;

import fr.gouv.tac.analytics.server.service.kafka.model.AnalyticsDeletion;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static fr.gouv.tac.analytics.server.test.KafkaMatchers.recentAnalyticsDeleteMessageWithInstallationUuid;
import static org.assertj.core.api.Assertions.assertThat;

public class KafkaMatchersTest {

    KafkaMatchers.KafkaDeleteAnalyticsMessageCondition matcher = recentAnalyticsDeleteMessageWithInstallationUuid(UUID.fromString("b47b9ab0-c818-4edd-9a2b-14bb8e905221"));

    @Test
    void can_detect_a_match() {
        AnalyticsDeletion analyticsDeletion = AnalyticsDeletion.builder()
                .installationUuid(UUID.fromString("b47b9ab0-c818-4edd-9a2b-14bb8e905221"))
                .timestamp(Instant.now().minusSeconds(1))
                .build();

        assertThat(matcher.matches(analyticsDeletion))
                .as("%s\nshould match %s", analyticsDeletion, matcher.description())
                .isTrue();
    }

    @Test
    void can_detect_installationUuid_mismatch() {
        final var valueWithInvalidUuid = AnalyticsDeletion.builder()
                .installationUuid(UUID.fromString("2e0b9796-a649-486f-9868-3c3f0935c59a"))
                .timestamp(Instant.now())
                .build();

        assertThat(matcher.matches(valueWithInvalidUuid))
                .as("%s\nshouldn't match %s", valueWithInvalidUuid, matcher.description())
                .isFalse();
    }

    @Test
    void can_detect_wrong_timestamp_too_old() {
        final var valueWithTooOldTimestamp = AnalyticsDeletion.builder()
                .installationUuid(UUID.fromString("b47b9ab0-c818-4edd-9a2b-14bb8e905221"))
                .timestamp(Instant.now().minusSeconds(6))
                .build();

        assertThat(matcher.matches(valueWithTooOldTimestamp))
                .as("%s\nshouldn't match %s", valueWithTooOldTimestamp, matcher.description())
                .isFalse();
    }

    @Test
    void can_detect_wrong_timestamp_in_future() {
        final var valueWithTooYoungTimestamp = AnalyticsDeletion.builder()
                .installationUuid(UUID.fromString("b47b9ab0-c818-4edd-9a2b-14bb8e905221"))
                .timestamp(Instant.now().plusSeconds(1))
                .build();

        assertThat(matcher.matches(valueWithTooYoungTimestamp))
                .as("%s\nshouldn't match %s", valueWithTooYoungTimestamp, matcher.description())
                .isFalse();
    }
}
