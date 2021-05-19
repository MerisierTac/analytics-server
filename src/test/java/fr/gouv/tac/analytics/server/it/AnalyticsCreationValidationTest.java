package fr.gouv.tac.analytics.server.it;


import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.UnsupportedEncodingException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.concurrent.ListenableFuture;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.tac.analytics.server.AnalyticsServerApplication;
import fr.gouv.tac.analytics.server.api.model.AnalyticsRequest;
import fr.gouv.tac.analytics.server.api.model.ErrorResponse;
import fr.gouv.tac.analytics.server.api.model.TimestampedEvent;
import fr.gouv.tac.analytics.server.model.kafka.Analytics;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

@ActiveProfiles(value = "test")
@SpringBootTest(classes = AnalyticsServerApplication.class)
@AutoConfigureMockMvc
public class AnalyticsCreationValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private KafkaTemplate<String, Analytics> kafkaTemplate;

    @Mock
    private ListenableFuture<SendResult<String, Analytics>> listenableFutureMock;

    /****************
     * ROOT
     ******/

    @Test
    @WithMockUser
    public void itShouldAcceptValidAnalytics() throws Exception {

        final AnalyticsRequest analyticsRequest = buildAnalytics();
        final String analyticsAsJson = objectMapper.writeValueAsString(analyticsRequest);

        when(kafkaTemplate.sendDefault(any(Analytics.class))).thenReturn(listenableFutureMock);

        mockMvc.perform(post("/api/v1/analytics")
                .contentType(APPLICATION_JSON)
                .content(analyticsAsJson))
                .andExpect(status().isOk())
                .andExpect(content().string(is(emptyString())));
    }

    @Test
    @WithMockUser
    public void itShouldRejectAnalyticsWithoutInstallationUuid() throws Exception {

        final AnalyticsRequest analyticsRequest = buildAnalytics();
        analyticsRequest.setInstallationUuid(null);

        final String analyticsAsJson = objectMapper.writeValueAsString(analyticsRequest);

        final MvcResult mvcResult = mockMvc.perform(post("/api/v1/analytics")
                .contentType(APPLICATION_JSON)
                .content(analyticsAsJson))
                .andExpect(status().isBadRequest())
                .andReturn();

        checkResult(mvcResult, "[Field error in object 'analyticsRequest' on field 'installationUuid': rejected value [null]", OffsetDateTime.now(ZoneId.of("UTC")));
    }

    @Test
    @WithMockUser
    public void itShouldRejectAnalyticsWithEmptyInstallationUuid() throws Exception {

        final AnalyticsRequest analyticsRequest = buildAnalytics();
        analyticsRequest.setInstallationUuid("");

        final String analyticsAsJson = objectMapper.writeValueAsString(analyticsRequest);

        final MvcResult mvcResult = mockMvc.perform(post("/api/v1/analytics")
                .contentType(APPLICATION_JSON)
                .content(analyticsAsJson))
                .andExpect(status().isBadRequest())
                .andReturn();

        checkResult(mvcResult, "[Field error in object 'analyticsRequest' on field 'installationUuid': rejected value []", OffsetDateTime.now(ZoneId.of("UTC")));
    }


    /****************
     * EVENT
     ******/

    @Test
    @WithMockUser
    public void itShouldRejectAnalyticsWithEmptyEventName() throws Exception {

        final AnalyticsRequest analyticsRequest = buildAnalytics();
        analyticsRequest.getEvents().get(0).setName("");

        final String analyticsAsJson = objectMapper.writeValueAsString(analyticsRequest);

        final MvcResult mvcResult = mockMvc.perform(post("/api/v1/analytics")
                .contentType(APPLICATION_JSON)
                .content(analyticsAsJson))
                .andExpect(status().isBadRequest())
                .andReturn();

        checkResult(mvcResult, "[Field error in object 'analyticsRequest' on field 'events[0].name': rejected value []", OffsetDateTime.now(ZoneId.of("UTC")));
    }


    @Test
    @WithMockUser
    public void itShouldRejectAnalyticsWithoutEventTimeStamp() throws Exception {

        final AnalyticsRequest analyticsRequest = buildAnalytics();
        analyticsRequest.getEvents().get(0).setTimestamp(null);

        final String analyticsAsJson = objectMapper.writeValueAsString(analyticsRequest);

        final MvcResult mvcResult = mockMvc.perform(post("/api/v1/analytics")
                .contentType(APPLICATION_JSON)
                .content(analyticsAsJson))
                .andExpect(status().isBadRequest())
                .andReturn();

        checkResult(mvcResult, "[Field error in object 'analyticsRequest' on field 'events[0].timestamp': rejected value [null]", OffsetDateTime.now(ZoneId.of("UTC")) );
    }

    /****************
     * ERROR
     ******/


    @Test
    @WithMockUser
    public void itShouldRejectAnalyticsWithEmptyErrorName() throws Exception {

        final AnalyticsRequest analyticsRequest = buildAnalytics();
        analyticsRequest.getErrors().get(0).setName("");

        final String analyticsAsJson = objectMapper.writeValueAsString(analyticsRequest);

        final MvcResult mvcResult = mockMvc.perform(post("/api/v1/analytics")
                .contentType(APPLICATION_JSON)
                .content(analyticsAsJson))
                .andExpect(status().isBadRequest())
                .andReturn();

        checkResult(mvcResult, "[Field error in object 'analyticsRequest' on field 'errors[0].name': rejected value []", OffsetDateTime.now(ZoneId.of("UTC")) );
    }

     @Test
    @WithMockUser
    public void itShouldRejectAnalyticsWithoutErrorTimeStamp() throws Exception {

        final AnalyticsRequest analyticsRequest = buildAnalytics();
         analyticsRequest.getErrors().get(0).setTimestamp(null);

        final String analyticsAsJson = objectMapper.writeValueAsString(analyticsRequest);

        final MvcResult mvcResult = mockMvc.perform(post("/api/v1/analytics")
                .contentType(APPLICATION_JSON)
                .content(analyticsAsJson))
                .andExpect(status().isBadRequest())
                .andReturn();

         checkResult(mvcResult, "[Field error in object 'analyticsRequest' on field 'errors[0].timestamp': rejected value [null]", OffsetDateTime.now(ZoneId.of("UTC")) );
     }

    private void checkResult(MvcResult mvcResult, String message, OffsetDateTime timestamp) throws com.fasterxml.jackson.core.JsonProcessingException, UnsupportedEncodingException {
        final ErrorResponse errorResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorResponse.class);
        assertThat(errorResponse.getMessage()).contains(message);
        assertThat(errorResponse.getTimestamp()).isEqualToIgnoringSeconds(timestamp);

        verify(kafkaTemplate, never()).sendDefault(any(Analytics.class));
    }

    private AnalyticsRequest buildAnalytics() {
        final Map<String, Object> infos = Map.of("info1", "info1Value", "info2", "info2value");

        final OffsetDateTime timestamp = OffsetDateTime.parse("2020-12-17T10:59:17.123Z");

        final TimestampedEvent event1 = TimestampedEvent.builder().name("eventName1").timestamp(timestamp).desc("event1 description").build();
        final TimestampedEvent event2 = TimestampedEvent.builder().name("eventName2").timestamp(timestamp).build();

        final TimestampedEvent error1 = TimestampedEvent.builder().name("errorName1").timestamp(timestamp).build();
        final TimestampedEvent error2 = TimestampedEvent.builder().name("errorName2").timestamp(timestamp).desc("error2 description").build();

        return AnalyticsRequest.builder()
                .installationUuid("some installation uuid")
                .infos(infos)
                .events(Arrays.asList(event1, event2))
                .errors(Arrays.asList(error1, error2))
                .build();
    }


}

