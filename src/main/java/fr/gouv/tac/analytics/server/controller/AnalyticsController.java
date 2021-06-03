package fr.gouv.tac.analytics.server.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.gouv.tac.analytics.server.api.AnalyticsApi;
import fr.gouv.tac.analytics.server.api.model.AnalyticsRequest;
import fr.gouv.tac.analytics.server.service.AnalyticsService;
import lombok.RequiredArgsConstructor;

import java.util.UUID;


@Controller
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AnalyticsController implements AnalyticsApi {

    private final AnalyticsService analyticsService;
    private final AnalyticsMapper analyticsMapper;

    @Override
    public ResponseEntity<Void> createAnalytics(AnalyticsRequest analyticsRequest) {
        analyticsService.createAnalytics(analyticsMapper.map(analyticsRequest));
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> deleteAnalytics(UUID installationUuid) {
        analyticsService.deleteAnalytics(installationUuid);
        return ResponseEntity.noContent().build();
    }
}
