package com.whereismymoney.backend.service;

import com.whereismymoney.backend.dto.PortfolioAnalyticsResponse;

public interface PortfolioAnalyticsService {

    PortfolioAnalyticsResponse getAnalytics(
            Long portfolioId,
            Long userId);
}