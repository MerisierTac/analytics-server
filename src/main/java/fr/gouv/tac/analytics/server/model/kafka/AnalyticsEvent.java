package fr.gouv.tac.analytics.server.model.kafka;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;

@Value
@Builder
public class AnalyticsEvent {

    String name;

    OffsetDateTime timestamp;

    String desc;
}
