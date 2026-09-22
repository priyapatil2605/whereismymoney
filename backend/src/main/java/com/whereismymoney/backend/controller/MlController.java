package com.whereismymoney.backend.controller;

import com.whereismymoney.backend.service.MlPredictionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ml")
public class MlController {

    private final MlPredictionService mlPredictionService;

    public MlController(
            MlPredictionService mlPredictionService) {

        this.mlPredictionService = mlPredictionService;
    }

    @GetMapping("/market/{symbol}")
    public ResponseEntity<String> getMarketData(
            @PathVariable String symbol) {

        return ResponseEntity.ok(
                mlPredictionService
                        .getMarketData(symbol));
    }

    @GetMapping("/features/{symbol}")
    public ResponseEntity<String> getFeatures(
            @PathVariable String symbol) {

        return ResponseEntity.ok(
                mlPredictionService
                        .getFeatures(symbol));
    }

    @PostMapping("/predict/{symbol}")
    public ResponseEntity<String> predict(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "5y") String period,
            @RequestParam(defaultValue = "1") int horizon) {

        return ResponseEntity.ok(
                mlPredictionService.predict(
                        symbol,
                        period,
                        horizon));
    }

    @GetMapping("/walk-forward/{symbol}")
    public ResponseEntity<String> walkForward(
            @PathVariable String symbol) {

        return ResponseEntity.ok(
                mlPredictionService
                        .getWalkForwardValidation(
                                symbol));
    }

    @PostMapping("/advanced-backtest/{symbol}")
    public ResponseEntity<String> advancedBacktest(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "5y") String period,
            @RequestParam(defaultValue = "100000") double initialCapital,
            @RequestParam(defaultValue = "10") double transactionCostBps,
            @RequestParam(defaultValue = "20") int fastWindow,
            @RequestParam(defaultValue = "50") int slowWindow) {

        return ResponseEntity.ok(
                mlPredictionService.advancedBacktest(
                        symbol,
                        period,
                        initialCapital,
                        transactionCostBps,
                        fastWindow,
                        slowWindow));
    }
}