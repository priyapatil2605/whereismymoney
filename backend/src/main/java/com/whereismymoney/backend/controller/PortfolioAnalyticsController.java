package com.whereismymoney.backend.controller;

import com.whereismymoney.backend.dto.PortfolioAnalyticsResponse;
import com.whereismymoney.backend.service.PortfolioAnalyticsService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/portfolios")
public class PortfolioAnalyticsController {

    private final PortfolioAnalyticsService analyticsService;

    public PortfolioAnalyticsController(
            PortfolioAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/{portfolioId}/analytics")
    public PortfolioAnalyticsResponse getAnalytics(
            @PathVariable Long portfolioId,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        return analyticsService.getAnalytics(
                portfolioId,
                userId);
    }
}