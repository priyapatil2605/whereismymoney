package com.whereismymoney.backend.dto;

import java.math.BigDecimal;

public record HoldingResponse(

        Long id,

        Long assetId,

        String symbol,

        String assetName,

        String assetType,

        BigDecimal quantity,

        BigDecimal averageBuyPrice,

        BigDecimal currentPrice,

        BigDecimal marketValue,

        BigDecimal costBasis,

        BigDecimal unrealizedPnl,

        BigDecimal unrealizedPnlPercent) {
}