package fr.gouv.tac.analytics.server.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.tac.analytics.server.AnalyticsServerApplication;
import fr.gouv.tac.analytics.server.api.model.ErrorResponse;
import fr.gouv.tac.analytics.server.model.kafka.AnalyticsDeletion;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
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

import java.io.UnsupportedEncodingException;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles(value = "test")
@SpringBootTest(classes = AnalyticsServerApplication.class)
@AutoConfigureMockMvc
public class AnalyticsDeletionValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private KafkaTemplate<String, AnalyticsDeletion> deleteKafkaTemplate;

    @Mock
    private ListenableFuture<SendResult<String, AnalyticsDeletion>> listenableFutureMock;

    @Test
    @WithMockUser
    public void itShouldRejectAnalyticsDeletionWithoutInstallationUuid() throws Exception {
        final MvcResult mvcResult = mockMvc.perform(delete("/api/v1/analytics")).andExpect(status().isBadRequest())
                .andReturn();

        checkResult(
                mvcResult, "Required String parameter 'installationUuid' is not present",
                OffsetDateTime.now(ZoneId.of("UTC"))
        );
    }

    @Test
    @WithMockUser
    public void itShouldRejectAnalyticsDeletionWithEmptyInstallationUuid() throws Exception {
        final MvcResult mvcResult = mockMvc.perform(delete("/api/v1/analytics").queryParam("installationUuid", ""))
                .andExpect(status().isBadRequest()).andReturn();

        checkResult(
                mvcResult, "deleteAnalytics.installationUuid: size must be between 1 and 64",
                OffsetDateTime.now(ZoneId.of("UTC"))
        );
    }

    private void checkResult(MvcResult mvcResult, String message, OffsetDateTime timestamp)
            throws com.fasterxml.jackson.core.JsonProcessingException, UnsupportedEncodingException {
        final ErrorResponse errorResponse = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                ErrorResponse.class
        );
        assertThat(errorResponse.getMessage()).contains(message);
        assertThat(errorResponse.getTimestamp()).isEqualToIgnoringSeconds(timestamp);

        verify(deleteKafkaTemplate, never()).sendDefault(any(AnalyticsDeletion.class));
    }
}
