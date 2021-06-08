package fr.gouv.tac.analytics.server.model.kafka;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Value
@Builder
public class Analytics {

    String installationUuid;

    Map<String, Object> infos;

    List<AnalyticsEvent> events;

    List<AnalyticsEvent> errors;

    OffsetDateTime creationDate = OffsetDateTime.now();
}
