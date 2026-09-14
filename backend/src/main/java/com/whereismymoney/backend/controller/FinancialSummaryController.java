package com.whereismymoney.backend.controller;

import com.whereismymoney.backend.dto.FinancialSummaryResponse;
import com.whereismymoney.backend.service.FinancialSummaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/financial-summary")
public class FinancialSummaryController {

    private final FinancialSummaryService financialSummaryService;

    public FinancialSummaryController(
            FinancialSummaryService financialSummaryService) {

        this.financialSummaryService = financialSummaryService;
    }

    @GetMapping("/{portfolioId}")
    public ResponseEntity<FinancialSummaryResponse> getSummary(
            @PathVariable Long portfolioId,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        return ResponseEntity.ok(
                financialSummaryService.getSummary(
                        portfolioId,
                        userId));
    }
}