package fr.gouv.tac.analytics.server.controller.mapper;

import fr.gouv.tac.analytics.server.api.model.TimestampedEvent;
import fr.gouv.tac.analytics.server.model.kafka.AnalyticsEvent;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TimestampedEventMapper {

    AnalyticsEvent map(TimestampedEvent timestampedEvent);
}
