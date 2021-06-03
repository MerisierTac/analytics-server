package fr.gouv.tac.analytics.server.service.kafka.model;

import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@AllArgsConstructor
@Builder
public class AnalyticsEvent {

    String name;

    OffsetDateTime timestamp;

    String desc;
}
