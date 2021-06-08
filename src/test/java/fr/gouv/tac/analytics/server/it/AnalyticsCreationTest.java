package fr.gouv.tac.analytics.server.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.tac.analytics.server.AnalyticsServerApplication;
import fr.gouv.tac.analytics.server.api.model.AnalyticsRequest;
import fr.gouv.tac.analytics.server.api.model.TimestampedEvent;
import fr.gouv.tac.analytics.server.config.AnalyticsProperties;
import fr.gouv.tac.analytics.server.model.kafka.Analytics;
import fr.gouv.tac.analytics.server.model.kafka.AnalyticsEvent;
import fr.gouv.tac.analytics.server.utils.TestUtils;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles(value = "test")
@SpringBootTest(classes = AnalyticsServerApplication.class)
@AutoConfigureMockMvc
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9094", "port=9094" }, topics = {
        "${analytics.creation-topic}", "${analytics.deletion-topic}" })
public class AnalyticsCreationTest {

    private static final int QUEUE_READ_TIMEOUT = 2;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private AnalyticsProperties analyticsProperties;

    @Autowired
    private KafkaProperties kafkaProperties;

    private KafkaMessageListenerContainer<String, Analytics> container;

    private final List<Analytics> records = new ArrayList<>();

    @BeforeEach
    public void setUp() {
        records.clear();
        final Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(
                kafkaProperties.getConsumer().getGroupId(),
                "false", embeddedKafkaBroker
        );
        final DefaultKafkaConsumerFactory<String, Analytics> defaultKafkaConsumerFactory = new DefaultKafkaConsumerFactory<>(
                consumerProps, new StringDeserializer(), new JsonDeserializer<>(Analytics.class, objectMapper)
        );
        final ContainerProperties containerProperties = new ContainerProperties(analyticsProperties.getCreationTopic());
        container = new KafkaMessageListenerContainer<>(defaultKafkaConsumerFactory, containerProperties);
        container.setupMessageListener((MessageListener<String, Analytics>) message -> records.add(message.value()));
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
    }

    @AfterEach
    public void tearDown() {
        container.stop();
    }

    @Test
    @WithMockUser
    public void itShouldStoreValidAnalytics() throws Exception {
        final AnalyticsRequest analyticsRequest = buildAnalyticsRequest();

        final List<AnalyticsEvent> expectedEvents = analyticsRequest.getEvents().stream()
                .map(TestUtils::convertTimestampedEvent).collect(Collectors.toList());

        final List<AnalyticsEvent> expectedErrors = analyticsRequest.getErrors().stream()
                .map(TestUtils::convertTimestampedEvent).collect(Collectors.toList());

        final String analyticsAsJson = objectMapper.writeValueAsString(analyticsRequest);

        // WHEN
        mockMvc.perform(post("/api/v1/analytics").contentType(APPLICATION_JSON).content(analyticsAsJson))
                .andExpect(status().isOk()).andExpect(content().string(is(emptyString())));

        await().atMost(QUEUE_READ_TIMEOUT, SECONDS).untilAsserted(() -> assertThat(records).isNotEmpty());

        assertThat(records).hasSize(1);
        final Analytics analyticsResult = records.get(0);

        assertThat(analyticsResult.getCreationDate()).isEqualToIgnoringSeconds(OffsetDateTime.now(ZoneId.of("UTC")));

        assertThat(analyticsResult.getInstallationUuid()).isEqualTo(analyticsRequest.getInstallationUuid());
        assertThat(analyticsResult.getInfos()).containsExactlyInAnyOrderEntriesOf(analyticsRequest.getInfos());
        assertThat(analyticsResult.getEvents()).containsExactlyInAnyOrderElementsOf(expectedEvents);
        assertThat(analyticsResult.getErrors()).containsExactlyInAnyOrderElementsOf(expectedErrors);
    }

    private AnalyticsRequest buildAnalyticsRequest() {
        final Map<String, Object> infos = Map.of(
                "infoString", "info1Value", "infoInteger", 2, "infoFloat", 1.03,
                "infoBoolean", true
        );

        final OffsetDateTime timestamp = OffsetDateTime.parse("2020-12-17T10:59:17.123Z");

        final TimestampedEvent event1 = TimestampedEvent.builder().name("eventName1").timestamp(timestamp)
                .desc("event1 description").build();
        final TimestampedEvent event2 = TimestampedEvent.builder().name("eventName2").timestamp(timestamp).build();

        final TimestampedEvent error1 = TimestampedEvent.builder().name("errorName1").timestamp(timestamp).build();
        final TimestampedEvent error2 = TimestampedEvent.builder().name("errorName2").timestamp(timestamp)
                .desc("error2 description").build();

        return AnalyticsRequest.builder().installationUuid("some installation uuid").infos(infos)
                .events(Arrays.asList(event1, event2)).errors(Arrays.asList(error1, error2)).build();
    }
}
