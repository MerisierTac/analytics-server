package fr.gouv.tac.analytics.server.service.kafka.model;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AnalyticsDeletion {

    UUID installationUuid;

    Instant timestamp;
}
