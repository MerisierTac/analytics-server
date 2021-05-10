package fr.gouv.tac.analytics.server.model.kafka;

import java.time.Instant;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AnalyticsDeletion {

    String installationUuid;

    Instant deletionTimeStamp;
}
