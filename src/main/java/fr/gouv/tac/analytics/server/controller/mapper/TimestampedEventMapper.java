package fr.gouv.tac.analytics.server.controller.mapper;

import org.mapstruct.Mapper;

import fr.gouv.tac.analytics.server.api.model.TimestampedEvent;
import fr.gouv.tac.analytics.server.model.kafka.AnalyticsEvent;

@Mapper(componentModel = "spring")
public interface TimestampedEventMapper {

    AnalyticsEvent map(TimestampedEvent timestampedEvent);
}
