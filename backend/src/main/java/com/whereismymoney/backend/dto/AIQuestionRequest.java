package com.whereismymoney.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AIQuestionRequest(
                @NotNull Long portfolioId,
                @NotBlank String question) {
}