package fr.gouv.tac.analytics.server.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static fr.gouv.tac.analytics.server.test.KafkaRecordAssert.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.equalTo;

public class KafkaRecordAssertTest {

    private ConsumerRecord<String, JsonNode> exampleConsumerRecord;

    @BeforeEach
    void setup() throws IOException {
        final var jsonValue = new ObjectMapper().readTree("{\"name\": \"Robert\"}");
        exampleConsumerRecord = new ConsumerRecord<>("topic-name", 0, 0, "key", jsonValue);
        exampleConsumerRecord.headers().add("TestHeader", "HeaderValue".getBytes(UTF_8));
    }

    @Test
    void can_detect_hasNoKey_mismatch() {
        assertThatThrownBy(() ->
                assertThat(exampleConsumerRecord).hasNoKey()
        )
                .hasMessage("[Kafka record shouldn't have a key] \n" +
                        "Expecting:\n <\"key\">\n" +
                        "to be equal to:\n <null>\n" +
                        "but was not.");
    }

    @Test
    void can_detect_hasNoHeader_mismatch() {
        assertThatThrownBy(() ->
                assertThat(exampleConsumerRecord).hasNoHeader("TestHeader")
        )
                .hasMessage("[Kafka record shouldn't have a 'TestHeader' header] \n" +
                        "Expecting empty but was:<[\"HeaderValue\"]>");
    }

    @Test
    void can_detect_hasJsonValue_mismatch() {
        assertThatThrownBy(() ->
                assertThat(exampleConsumerRecord).hasJsonValue("name", "clea")
        )
                .hasMessage("\nExpecting:\n  <\"Robert\">\n"
                        + "to satisfy:\n  <\"clea\">");
    }

    @Test
    void can_detect_hasJsonValue_mismatch_with_matcher() {
        assertThatThrownBy(() ->
                assertThat(exampleConsumerRecord).hasJsonValue("name", equalTo("clea"))
        )
                .hasMessage("\nExpecting:\n  <\"Robert\">\n"
                        + "to satisfy:\n  <\"clea\">");
    }
}
