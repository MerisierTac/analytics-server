package fr.gouv.tac.analytics.server.service.kafka.model;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.With;

@Value
@AllArgsConstructor
@Builder
public class AnalyticsCreation {

    String installationUuid;

    Map<String, Object> infos;

    List<AnalyticsEvent> events;

    List<AnalyticsEvent> errors;

    @With
    OffsetDateTime creationDate;
}
