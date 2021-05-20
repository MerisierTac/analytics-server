package fr.gouv.tac.analytics.server.controller.mapper;

import org.mapstruct.Mapper;

import fr.gouv.tac.analytics.server.api.model.AnalyticsRequest;
import fr.gouv.tac.analytics.server.model.kafka.Analytics;

@Mapper(componentModel = "spring", uses = TimestampedEventMapper.class)
public interface AnalyticsMapper {

    Analytics map(final AnalyticsRequest analyticsRequest);
}
