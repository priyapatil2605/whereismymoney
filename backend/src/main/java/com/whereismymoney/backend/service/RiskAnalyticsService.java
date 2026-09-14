package com.whereismymoney.backend.service;

import com.whereismymoney.backend.entity.Transaction;
import com.whereismymoney.backend.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class RiskAnalyticsService {

    private final TransactionRepository transactionRepository;
    private final OwnershipService ownershipService;

    public RiskAnalyticsService(
            TransactionRepository transactionRepository,
            OwnershipService ownershipService) {

        this.transactionRepository = transactionRepository;
        this.ownershipService = ownershipService;
    }

    public Map<String, Object> calculateRisk(
            Long portfolioId,
            Long userId) {

        ownershipService.getOwnedPortfolio(
                portfolioId,
                userId);

        List<Transaction> transactions = transactionRepository
                .findByPortfolioIdOrderByTransactionDateDesc(
                        portfolioId);

        List<Double> values = transactions.stream()
                .map(t -> t.getTotalAmount()
                        .doubleValue())
                .toList();

        double mean = values.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0);

        double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average()
                .orElse(0);

        double volatility = Math.sqrt(variance);

        double maxDrawdown = 0;
        double peak = 0;
        double cumulative = 0;

        for (double value : values) {

            cumulative += value;

            peak = Math.max(
                    peak,
                    cumulative);

            if (peak > 0) {

                double drawdown = (peak - cumulative)
                        / peak;

                maxDrawdown = Math.max(
                        maxDrawdown,
                        drawdown);
            }
        }

        double var95 = calculateVaR(values, 0.95);

        Map<String, Object> result = new LinkedHashMap<>();

        result.put(
                "portfolioId",
                portfolioId);

        result.put(
                "transactionCount",
                values.size());

        result.put(
                "volatility",
                volatility);

        result.put(
                "maxDrawdown",
                maxDrawdown);

        result.put(
                "valueAtRisk95",
                var95);

        result.put(
                "riskLevel",
                determineRiskLevel(
                        volatility,
                        maxDrawdown));

        return result;
    }

    private double calculateVaR(
            List<Double> values,
            double confidence) {

        if (values.isEmpty()) {
            return 0;
        }

        List<Double> sorted = new ArrayList<>(values);

        Collections.sort(sorted);

        int index = (int) Math.floor(
                (1 - confidence)
                        * sorted.size());

        index = Math.max(
                0,
                Math.min(
                        index,
                        sorted.size() - 1));

        return Math.abs(
                sorted.get(index));
    }

    private String determineRiskLevel(
            double volatility,
            double drawdown) {

        if (volatility > 10000
                || drawdown > 0.30) {

            return "HIGH";
        }

        if (volatility > 5000
                || drawdown > 0.15) {

            return "MEDIUM";
        }

        return "LOW";
    }
}