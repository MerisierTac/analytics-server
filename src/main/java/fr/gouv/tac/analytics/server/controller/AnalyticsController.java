package fr.gouv.tac.analytics.server.controller;

import fr.gouv.tac.analytics.server.api.AnalyticsApi;
import fr.gouv.tac.analytics.server.api.model.AnalyticsRequest;
import fr.gouv.tac.analytics.server.controller.mapper.AnalyticsMapper;
import fr.gouv.tac.analytics.server.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(path = "/api/v1")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsController implements AnalyticsApi {

    private final AnalyticsService analyticsService;

    private final AnalyticsMapper analyticsMapper;

    @Override
    public ResponseEntity<Void> createAnalytics(AnalyticsRequest analyticsRequest) {
        analyticsService.createAnalytics(analyticsMapper.map(analyticsRequest));
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> deleteAnalytics(String installationUuid) {
        log.info("Analytics deletion order has been received from mobile application : {}", installationUuid);
        analyticsService.deleteAnalytics(installationUuid);
        return ResponseEntity.noContent().build();
    }
}
