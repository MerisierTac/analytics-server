package fr.gouv.tac.analytics.server.controller;

import fr.gouv.tac.analytics.server.api.model.AnalyticsRequest;
import fr.gouv.tac.analytics.server.api.model.TimestampedEvent;
import fr.gouv.tac.analytics.server.service.kafka.model.AnalyticsCreation;
import fr.gouv.tac.analytics.server.service.kafka.model.AnalyticsEvent;
import org.mapstruct.Mapper;

@Mapper
public interface AnalyticsMapper {

    AnalyticsCreation map(final AnalyticsRequest analyticsRequest);

    AnalyticsEvent map(TimestampedEvent timestampedEvent);
}