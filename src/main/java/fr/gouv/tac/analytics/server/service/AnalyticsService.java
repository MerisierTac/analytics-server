package fr.gouv.tac.analytics.server.service;

import java.time.Instant;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFutureCallback;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import fr.gouv.tac.analytics.server.model.kafka.Analytics;
import fr.gouv.tac.analytics.server.model.kafka.AnalyticsDeletion;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final KafkaTemplate<String, Analytics> creationKafkaTemplate;

    private final KafkaTemplate<String, AnalyticsDeletion> deletionKafkaTemplate;

    public void createAnalytics(final Analytics analytics) {
        creationKafkaTemplate.sendDefault(analytics).addCallback(new ListenableFutureCallback<>() {

            @Override
            public void onFailure(final Throwable throwable) {
                log.warn("Analytics creation - error sending message to kafka", throwable);
            }

            @Override
            public void onSuccess(final SendResult<String, Analytics> sendResult) {
                log.debug("Message successfully sent {}", sendResult);
            }
        });
    }

    public void deleteAnalytics(final String installationUuid) {
        AnalyticsDeletion analyticsDeletion = AnalyticsDeletion.builder().installationUuid(installationUuid)
                .deletionTimeStamp(Instant.now()).build();

        deletionKafkaTemplate.sendDefault(analyticsDeletion).addCallback(new ListenableFutureCallback<>() {

            @Override
            public void onFailure(final Throwable throwable) {
                log.warn("Analytics deletion - error sending message to kafka", throwable);
            }

            @Override
            public void onSuccess(final SendResult<String, AnalyticsDeletion> sendResult) {
                log.debug("Message successfully sent {}", sendResult);
            }
        });
    }
}
