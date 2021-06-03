package fr.gouv.tac.analytics.server.service.kafka.model;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@AllArgsConstructor
@Builder
public class AnalyticsCreation {

    UUID installationUuid;

    Map<String, Object> infos;

    List<AnalyticsEvent> events;

    List<AnalyticsEvent> errors;
}
