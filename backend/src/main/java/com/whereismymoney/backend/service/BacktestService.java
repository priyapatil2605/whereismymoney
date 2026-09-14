package com.whereismymoney.backend.service;

import com.whereismymoney.backend.entity.Transaction;
import com.whereismymoney.backend.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BacktestService {

    private final TransactionRepository transactionRepository;
    private final OwnershipService ownershipService;

    public BacktestService(
            TransactionRepository transactionRepository,
            OwnershipService ownershipService) {

        this.transactionRepository = transactionRepository;

        this.ownershipService = ownershipService;
    }

    public Map<String, Object> runBacktest(
            Long portfolioId,
            Long userId) {

        ownershipService.getOwnedPortfolio(
                portfolioId,
                userId);

        List<Transaction> transactions = transactionRepository
                .findByPortfolioIdOrderByTransactionDateDesc(
                        portfolioId);

        double capital = 100000.0;
        double startingCapital = capital;

        int trades = 0;

        for (Transaction transaction : transactions) {

            double amount = transaction
                    .getTotalAmount()
                    .doubleValue();

            if (transaction.getTransactionType() == Transaction.TransactionType.BUY) {

                capital -= amount;
                trades++;

            } else {

                capital += amount;
                trades++;
            }
        }

        double returnPercentage = startingCapital == 0
                ? 0
                : ((capital - startingCapital)
                        / startingCapital) * 100;

        Map<String, Object> result = new LinkedHashMap<>();

        result.put(
                "startingCapital",
                startingCapital);

        result.put(
                "endingCapital",
                capital);

        result.put(
                "totalReturn",
                capital - startingCapital);

        result.put(
                "returnPercentage",
                returnPercentage);

        result.put(
                "trades",
                trades);

        result.put(
                "strategy",
                "Historical transaction replay");

        return result;
    }
}