package fr.gouv.tac.analytics.server.controller;

import fr.gouv.tac.analytics.server.api.model.AnalyticsRequest;
import fr.gouv.tac.analytics.server.api.model.TimestampedEvent;
import fr.gouv.tac.analytics.server.service.kafka.model.AnalyticsCreation;
import fr.gouv.tac.analytics.server.service.kafka.model.AnalyticsEvent;
import org.mapstruct.MapMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface AnalyticsMapper {

    @Mapping(target = "creationDate", ignore = true)
    AnalyticsCreation map(AnalyticsRequest analyticsRequest);

    AnalyticsEvent map(TimestampedEvent timestampedEvent);
}
