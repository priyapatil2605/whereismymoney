package com.whereismymoney.backend.controller;

import com.whereismymoney.backend.dto.AIQuestionRequest;
import com.whereismymoney.backend.service.FinancialInsightService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final FinancialInsightService financialInsightService;

    public AIController(
            FinancialInsightService financialInsightService) {

        this.financialInsightService = financialInsightService;
    }

    @PostMapping("/ask")
    public ResponseEntity<Map<String, String>> askQuestion(
            @Valid @RequestBody AIQuestionRequest request) {

        String answer = financialInsightService.answerQuestion(
                request.portfolioId(),
                request.question());

        return ResponseEntity.ok(
                Map.of(
                        "question",
                        request.question(),
                        "answer",
                        answer));
    }
}