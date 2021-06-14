package fr.gouv.tac.analytics.server.test;

import fr.gouv.tac.analytics.server.api.model.AnalyticsRequest;
import fr.gouv.tac.analytics.server.api.model.AnalyticsRequest.AnalyticsRequestBuilder;
import fr.gouv.tac.analytics.server.api.model.TimestampedEvent;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ExampleData {

    public static AnalyticsRequestBuilder analyticsRequest() {
        final Map<String, Object> infos = Map.of(
                "os", "Android",
                "type", 0,
                "load", 1.03,
                "root", false
        );
        final var timestamp = OffsetDateTime.parse("2020-12-17T10:59:17.123Z");
        return AnalyticsRequest.builder()
                .installationUuid(UUID.randomUUID().toString())
                .infos(infos)
                .events(List.of(
                        TimestampedEvent.builder()
                                .name("eventName1")
                                .timestamp(timestamp)
                                .desc("event1 description")
                                .build(),
                        TimestampedEvent.builder()
                                .name("eventName2")
                                .timestamp(timestamp)
                                .build()
                ))
                .errors(List.of(
                        TimestampedEvent.builder()
                                .name("errorName1")
                                .timestamp(timestamp)
                                .build(),
                        TimestampedEvent.builder()
                                .name("errorName2").
                                timestamp(timestamp)
                                .desc("error2 description")
                                .build()
                ));
    }
}
