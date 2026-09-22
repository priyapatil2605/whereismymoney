package com.whereismymoney.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record HoldingRequest(

        @NotNull Long assetId,

        @NotNull @DecimalMin("0.00000001") BigDecimal quantity,

        @NotNull @DecimalMin("0.0") BigDecimal averageBuyPrice,

        @DecimalMin("0.0") BigDecimal currentPrice) {
}