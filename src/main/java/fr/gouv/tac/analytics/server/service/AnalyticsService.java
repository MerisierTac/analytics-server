package fr.gouv.tac.analytics.server.service;

import fr.gouv.tac.analytics.server.config.AnalyticsProperties;
import fr.gouv.tac.analytics.server.service.kafka.model.AnalyticsCreation;
import fr.gouv.tac.analytics.server.service.kafka.model.AnalyticsDeletion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final AnalyticsProperties analyticsProperties;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void createAnalytics(final AnalyticsCreation analytics) {
        kafkaTemplate.send(analyticsProperties.getCreationTopic(), analytics)
                .addCallback(
                        sendResult -> log.debug("Message successfully sent {}", sendResult),
                        throwable -> log.warn("Analytics creation - error sending message to kafka", throwable)
                );
    }

    public void deleteAnalytics(final String installationUuid) {
        final var analyticsDeletion = AnalyticsDeletion.builder()
                .installationUuid(installationUuid)
                .deletionTimestamp(Instant.now())
                .build();

        kafkaTemplate.send(analyticsProperties.getDeletionTopic(), analyticsDeletion)
                .addCallback(
                        sendResult -> log.debug("Message successfully sent {}", sendResult),
                        throwable -> log.warn("Analytics deletion - error sending message to kafka", throwable)
                );
    }
}
