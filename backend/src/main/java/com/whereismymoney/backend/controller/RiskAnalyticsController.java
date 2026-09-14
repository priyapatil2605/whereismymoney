package com.whereismymoney.backend.controller;

import com.whereismymoney.backend.service.RiskAnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/risk")
public class RiskAnalyticsController {

    private final RiskAnalyticsService riskAnalyticsService;

    public RiskAnalyticsController(
            RiskAnalyticsService riskAnalyticsService) {

        this.riskAnalyticsService = riskAnalyticsService;
    }

    @GetMapping("/{portfolioId}")
    public ResponseEntity<Map<String, Object>> getRisk(
            @PathVariable Long portfolioId,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        return ResponseEntity.ok(
                riskAnalyticsService.calculateRisk(
                        portfolioId,
                        userId));
    }
}