package fr.gouv.tac.analytics.server.config.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.tac.analytics.server.config.AnalyticsProperties;
import fr.gouv.tac.analytics.server.model.kafka.Analytics;
import fr.gouv.tac.analytics.server.model.kafka.AnalyticsDeletion;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
@Slf4j
public class KafkaConfiguration {

    @Bean
    public KafkaTemplate<String, Analytics> creationKafkaTemplate(final ObjectMapper objectMapper,
            final ProducerFactory defaultSpringProducerFactory, final AnalyticsProperties analyticsProperties) {
        final ProducerFactory<String, Analytics> producerFactory = new DefaultKafkaProducerFactory<>(
                defaultSpringProducerFactory.getConfigurationProperties(), new StringSerializer(),
                new JsonSerializer<Analytics>(objectMapper)
        );

        final KafkaTemplate<String, Analytics> creationKafkaTemplate = new KafkaTemplate<>(producerFactory);
        creationKafkaTemplate.setDefaultTopic(analyticsProperties.getCreationTopic());
        return creationKafkaTemplate;
    }

    @Bean
    public KafkaTemplate<String, AnalyticsDeletion> deletionKafkaTemplate(final ObjectMapper objectMapper,
            final ProducerFactory defaultSpringProducerFactory, final AnalyticsProperties analyticsProperties) {
        final ProducerFactory<String, AnalyticsDeletion> producerFactory = new DefaultKafkaProducerFactory<>(
                defaultSpringProducerFactory.getConfigurationProperties(), new StringSerializer(),
                new JsonSerializer<AnalyticsDeletion>(objectMapper)
        );

        final KafkaTemplate<String, AnalyticsDeletion> deletionKafkaTemplate = new KafkaTemplate<>(producerFactory);
        deletionKafkaTemplate.setDefaultTopic(analyticsProperties.getDeletionTopic());
        return deletionKafkaTemplate;
    }
}
