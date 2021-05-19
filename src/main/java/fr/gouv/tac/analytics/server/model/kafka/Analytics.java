package fr.gouv.tac.analytics.server.model.kafka;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Analytics {

    String installationUuid;

    Map<String, Object> infos;

    List<AnalyticsEvent> events;

    List<AnalyticsEvent> errors;

    OffsetDateTime creationDate = OffsetDateTime.now();
}
