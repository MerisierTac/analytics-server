package fr.gouv.tac.analytics.server.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.tac.analytics.server.AnalyticsServerApplication;
import fr.gouv.tac.analytics.server.api.model.AnalyticsRequest;
import fr.gouv.tac.analytics.server.api.model.ErrorResponse;
import fr.gouv.tac.analytics.server.config.security.oauth2tokenvalidator.ExpirationTokenPresenceOAuth2TokenValidator;
import fr.gouv.tac.analytics.server.config.security.oauth2tokenvalidator.JtiPresenceOAuth2TokenValidator;
import fr.gouv.tac.analytics.server.model.kafka.Analytics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.concurrent.ListenableFuture;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles(value = "test")
@SpringBootTest(classes = AnalyticsServerApplication.class)
@AutoConfigureMockMvc
public class AnalyticsCreationOauth2ErrorTest {

    @MockBean
    private KafkaTemplate<String, Analytics> kafkaTemplate;

    @Mock
    private ListenableFuture<SendResult<String, Analytics>> listenableFutureMock;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${analytics.robert_jwt_analyticsprivatekey}")
    private String jwtPrivateKey;

    private JwtTokenHelper jwtTokenHelper;

    @BeforeEach
    public void setUp() throws InvalidKeySpecException, NoSuchAlgorithmException {
        jwtTokenHelper = new JwtTokenHelper(jwtPrivateKey);
    }

    @Test
    public void itShouldRejectWhenThereIsNoAuthenticationHeader() throws Exception {
        final AnalyticsRequest analyticsRequest = buildAnalyticsRequest();

        final String analyticsAsJson = objectMapper.writeValueAsString(analyticsRequest);

        final MvcResult mvcResult = mockMvc
                .perform(post("/api/v1/analytics").contentType(APPLICATION_JSON).content(analyticsAsJson))
                .andExpect(status().isUnauthorized()).andReturn();

        final ErrorResponse errorResponse = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                ErrorResponse.class
        );
        assertThat(errorResponse.getMessage()).isEqualTo("Full authentication is required to access this resource");
        assertThat(errorResponse.getTimestamp()).isEqualToIgnoringSeconds(OffsetDateTime.now(ZoneId.of("UTC")));

        verify(kafkaTemplate, never()).sendDefault(any(Analytics.class));
    }

    @Test
    public void itShouldRejectTokenWithoutJTI() throws Exception {
        final AnalyticsRequest analyticsRequest = buildAnalyticsRequest();
        final String analyticsAsJson = objectMapper.writeValueAsString(analyticsRequest);

        jwtTokenHelper.withIssueTime(ZonedDateTime.now());
        jwtTokenHelper.withExpirationDate(ZonedDateTime.now().plusMinutes(5));
        final String authorizationHeader = jwtTokenHelper.generateAuthorizationHeader();

        final MvcResult mvcResult = mockMvc.perform(
                post("/api/v1/analytics").header(AUTHORIZATION, authorizationHeader)
                        .contentType(APPLICATION_JSON).content(analyticsAsJson)
        ).andExpect(status().isUnauthorized())
                .andReturn();

        final ErrorResponse errorResponse = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                ErrorResponse.class
        );
        assertThat(errorResponse.getMessage()).contains(JtiPresenceOAuth2TokenValidator.ERR_MESSAGE);
        assertThat(errorResponse.getTimestamp()).isEqualToIgnoringSeconds(OffsetDateTime.now(ZoneId.of("UTC")));

        verify(kafkaTemplate, never()).sendDefault(any(Analytics.class));
    }

    @Test
    public void itShouldRejectTokenWithoutTokenExpiration() throws Exception {
        final AnalyticsRequest analyticsRequest = buildAnalyticsRequest();
        final String analyticsAsJson = objectMapper.writeValueAsString(analyticsRequest);

        jwtTokenHelper.withJti(UUID.randomUUID().toString());
        jwtTokenHelper.withIssueTime(ZonedDateTime.now().minusMinutes(10));

        final String authorizationHeader = jwtTokenHelper.generateAuthorizationHeader();

        final MvcResult mvcResult = mockMvc.perform(
                post("/api/v1/analytics").header(AUTHORIZATION, authorizationHeader)
                        .contentType(APPLICATION_JSON).content(analyticsAsJson)
        ).andExpect(status().isUnauthorized())
                .andReturn();

        final ErrorResponse errorResponse = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                ErrorResponse.class
        );
        assertThat(errorResponse.getMessage()).contains(ExpirationTokenPresenceOAuth2TokenValidator.ERR_MESSAGE);
        assertThat(errorResponse.getTimestamp()).isEqualToIgnoringSeconds(OffsetDateTime.now(ZoneId.of("UTC")));

        verify(kafkaTemplate, never()).sendDefault(any(Analytics.class));
    }

    @Test
    public void itShouldRejectExpiredToken() throws Exception {
        final AnalyticsRequest analyticsRequest = buildAnalyticsRequest();
        final String analyticsAsJson = objectMapper.writeValueAsString(analyticsRequest);

        jwtTokenHelper.withJti(UUID.randomUUID().toString());
        jwtTokenHelper.withIssueTime(ZonedDateTime.now().minusMinutes(10));
        jwtTokenHelper.withExpirationDate(ZonedDateTime.now().minusMinutes(2));
        final String authorizationHeader = jwtTokenHelper.generateAuthorizationHeader();

        final MvcResult mvcResult = mockMvc.perform(
                post("/api/v1/analytics").header(AUTHORIZATION, authorizationHeader)
                        .contentType(APPLICATION_JSON).content(analyticsAsJson)
        ).andExpect(status().isUnauthorized())
                .andReturn();

        final ErrorResponse errorResponse = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                ErrorResponse.class
        );
        assertThat(errorResponse.getMessage())
                .startsWith("An error occurred while attempting to decode the Jwt: Jwt expired at");
        assertThat(errorResponse.getTimestamp()).isEqualToIgnoringSeconds(OffsetDateTime.now(ZoneId.of("UTC")));

        verify(kafkaTemplate, never()).sendDefault(any(Analytics.class));
    }

    private AnalyticsRequest buildAnalyticsRequest() {
        return AnalyticsRequest.builder().installationUuid("some installation uuid").build();
    }
}
