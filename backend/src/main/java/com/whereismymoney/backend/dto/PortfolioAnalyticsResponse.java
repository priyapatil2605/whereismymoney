package com.whereismymoney.backend.dto;

import java.math.BigDecimal;
import java.util.List;

public record PortfolioAnalyticsResponse(
        BigDecimal totalCost,
        BigDecimal totalMarketValue,
        BigDecimal unrealizedPnl,
        BigDecimal unrealizedPnlPercent,
        BigDecimal concentrationPercent,
        List<Allocation> allocations) {

    public record Allocation(
            Long assetId,
            String symbol,
            String assetType,
            BigDecimal marketValue,
            BigDecimal allocationPercent) {
    }
}