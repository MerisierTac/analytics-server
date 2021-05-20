package fr.gouv.tac.analytics.server.utils;

import fr.gouv.tac.analytics.server.model.kafka.AnalyticsEvent;

public class TestUtils {

    public static AnalyticsEvent convertTimestampedEvent(
            final fr.gouv.tac.analytics.server.api.model.TimestampedEvent timestampedEventVo) {
        return AnalyticsEvent.builder().name(timestampedEventVo.getName()).timestamp(timestampedEventVo.getTimestamp())
                .desc(timestampedEventVo.getDesc()).build();
    }
}
