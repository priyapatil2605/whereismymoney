package com.whereismymoney.backend.controller;

import com.whereismymoney.backend.service.FinancialInsightService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiAnalystController {

    private final FinancialInsightService financialInsightService;

    public AiAnalystController(
            FinancialInsightService financialInsightService) {

        this.financialInsightService = financialInsightService;
    }

    @PostMapping("/ask/{portfolioId}")
    public ResponseEntity<AiAnalystResponse> askQuestion(
            @PathVariable Long portfolioId,
            @Valid @RequestBody AiAnalystRequest request) {

        String answer = financialInsightService.answerQuestion(
                portfolioId,
                request.question());

        return ResponseEntity.ok(
                new AiAnalystResponse(
                        portfolioId,
                        request.question(),
                        answer));
    }

    public record AiAnalystRequest(
            @NotNull @NotBlank String question) {
    }

    public record AiAnalystResponse(
            Long portfolioId,
            String question,
            String answer) {
    }
}