package fr.gouv.tac.analytics.server.controller.mapper;

import fr.gouv.tac.analytics.server.api.model.AnalyticsRequest;
import fr.gouv.tac.analytics.server.model.kafka.Analytics;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring", uses = TimestampedEventMapper.class)
public interface AnalyticsMapper {


    Analytics map(final AnalyticsRequest analyticsRequest);

}