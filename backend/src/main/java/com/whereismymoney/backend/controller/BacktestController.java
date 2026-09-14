package com.whereismymoney.backend.controller;

import com.whereismymoney.backend.service.BacktestService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/backtest")
public class BacktestController {

    private final BacktestService backtestService;

    public BacktestController(
            BacktestService backtestService) {

        this.backtestService = backtestService;
    }

    @PostMapping("/{portfolioId}")
    public ResponseEntity<Map<String, Object>> run(
            @PathVariable Long portfolioId,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        return ResponseEntity.ok(
                backtestService.runBacktest(
                        portfolioId,
                        userId));
    }
}