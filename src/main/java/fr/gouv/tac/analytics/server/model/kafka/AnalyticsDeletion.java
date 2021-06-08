package fr.gouv.tac.analytics.server.model.kafka;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class AnalyticsDeletion {

    String installationUuid;

    Instant deletionTimeStamp;
}
