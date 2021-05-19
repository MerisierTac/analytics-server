package fr.gouv.tac.analytics.server.model.kafka;

import java.time.OffsetDateTime;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AnalyticsEvent {

    String name;

    OffsetDateTime timestamp;

    String desc;
}
