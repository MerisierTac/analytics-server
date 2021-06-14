package fr.gouv.tac.analytics.server.controller;

import fr.gouv.tac.analytics.server.service.kafka.model.AnalyticsDeletion;
import fr.gouv.tac.analytics.server.test.IntegrationTest;
import fr.gouv.tac.analytics.server.test.KafkaManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static fr.gouv.tac.analytics.server.test.KafkaMatchers.recentAnalyticsDeleteMessageWithInstallationUuid;
import static fr.gouv.tac.analytics.server.test.RestAssuredManager.givenAuthenticated;
import static fr.gouv.tac.analytics.server.test.RestAssuredMatchers.isStringDateBetweenNowAndTenSecondAgo;
import static io.restassured.http.ContentType.JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@IntegrationTest
class AnalyticsControllerDeleteTest {

    @Autowired
    AnalyticsMapper analyticsMapper;

    @Test
    void can_send_a_delete_message_in_kafka() {
        final var installationUuid = UUID.randomUUID();

        givenAuthenticated()
                .delete("/api/v1/analytics?installationUuid={uuid}", installationUuid)

                .then()
                .statusCode(NO_CONTENT.value())
                .body(emptyString());

        assertThat(KafkaManager.awaitMessageValues(AnalyticsDeletion.class))
                .areExactly(1, recentAnalyticsDeleteMessageWithInstallationUuid(installationUuid.toString()))
                .hasSize(1);
    }

    @Test
    void can_send_multiple_delete_messages_in_kafka() {
        final var uuids = List.of(
                "8ef146ad-56c1-488a-af2a-99fd5b76b7bd",
                "21d76e63-7ab4-480e-9ba5-330ee63d38cf",
                "23498429-3c79-42a8-ac99-bc4c1742c2bd",
                "ca5739b5-3aad-4f7f-bcb2-5c5b99ffc2a9",
                "c33a4891-6d43-4fe3-95b6-d28a6bf30deb"
        );

        uuids.forEach(installationUuid -> {
            givenAuthenticated()
                    .delete("/api/v1/analytics?installationUuid={uuid}", installationUuid)

                    .then()
                    .statusCode(NO_CONTENT.value())
                    .body(emptyString());
        });

        assertThat(KafkaManager.awaitMessageValues(AnalyticsDeletion.class))
                .areExactly(1, recentAnalyticsDeleteMessageWithInstallationUuid("8ef146ad-56c1-488a-af2a-99fd5b76b7bd"))
                .areExactly(1, recentAnalyticsDeleteMessageWithInstallationUuid("21d76e63-7ab4-480e-9ba5-330ee63d38cf"))
                .areExactly(1, recentAnalyticsDeleteMessageWithInstallationUuid("23498429-3c79-42a8-ac99-bc4c1742c2bd"))
                .areExactly(1, recentAnalyticsDeleteMessageWithInstallationUuid("ca5739b5-3aad-4f7f-bcb2-5c5b99ffc2a9"))
                .areExactly(1, recentAnalyticsDeleteMessageWithInstallationUuid("c33a4891-6d43-4fe3-95b6-d28a6bf30deb"))
                .hasSize(5);
    }

    @Test
    void bad_request_on_missing_param_installationUuid() {
        givenAuthenticated()
                .delete("/api/v1/analytics")

                .then()
                .contentType(JSON)
                .statusCode(BAD_REQUEST.value())
                .body("status", equalTo(400))
                .body("error", equalTo("Bad Request"))
                .body("message", equalTo("Required String parameter 'installationUuid' is not present"))
                .body("timestamp", isStringDateBetweenNowAndTenSecondAgo())
                .body("path", equalTo("/api/v1/analytics"));
    }

    @Test
    void bad_request_on_empty_param_installationUuid() {
        givenAuthenticated()
                .delete("/api/v1/analytics?installationUuid=")

                .then()
                .contentType(JSON)
                .statusCode(BAD_REQUEST.value())
                .body("status", equalTo(400))
                .body("error", equalTo("Bad Request"))
                .body("message", equalTo("Request body contains invalid attributes"))
                .body("timestamp", isStringDateBetweenNowAndTenSecondAgo())
                .body("path", equalTo("/api/v1/analytics"))
                .body("errors", contains(Map.of(
                        "field", "deleteAnalytics.installationUuid",
                        "code", "Size",
                        "message", "size must be between 1 and 64"
                )));
    }

    @Test
    void bad_request_on_tooLong_param_installationUuid() {
        final var installationUuid = UUID.randomUUID();

        givenAuthenticated()
                .delete("/api/v1/analytics?installationUuid={uuid}", installationUuid.toString() + installationUuid.toString())

                .then()
                .contentType(JSON)
                .statusCode(BAD_REQUEST.value())
                .body("status", equalTo(400))
                .body("error", equalTo("Bad Request"))
                .body("message", equalTo("Request body contains invalid attributes"))
                .body("timestamp", isStringDateBetweenNowAndTenSecondAgo())
                .body("path", equalTo("/api/v1/analytics"))
                .body("errors", contains(Map.of(
                        "field", "deleteAnalytics.installationUuid",
                        "code", "Size",
                        "message", "size must be between 1 and 64"
                )));
    }
}
