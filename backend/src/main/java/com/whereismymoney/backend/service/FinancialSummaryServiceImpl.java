package com.whereismymoney.backend.service;

import com.whereismymoney.backend.dto.CategoryExpenseResponse;
import com.whereismymoney.backend.dto.FinancialSummaryResponse;
import com.whereismymoney.backend.entity.Transaction;
import com.whereismymoney.backend.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class FinancialSummaryServiceImpl
        implements FinancialSummaryService {

    private final TransactionRepository transactionRepository;
    private final OwnershipService ownershipService;

    public FinancialSummaryServiceImpl(
            TransactionRepository transactionRepository,
            OwnershipService ownershipService) {

        this.transactionRepository = transactionRepository;
        this.ownershipService = ownershipService;
    }

    @Override
    public FinancialSummaryResponse getSummary(
            Long portfolioId,
            Long userId) {

        ownershipService.getOwnedPortfolio(
                portfolioId,
                userId);

        List<Transaction> transactions = transactionRepository
                .findByPortfolioIdOrderByTransactionDateDesc(
                        portfolioId);

        BigDecimal totalIncome = transactionRepository.getTotalIncome(
                portfolioId);

        BigDecimal totalExpense = transactionRepository.getTotalExpense(
                portfolioId);

        BigDecimal netCashFlow = totalIncome.subtract(totalExpense);

        BigDecimal totalBuy = transactionRepository.totalBuyAmount(
                portfolioId);

        BigDecimal totalSell = transactionRepository.totalSellAmount(
                portfolioId);

        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);

        BigDecimal monthlyIncome = transactionRepository.totalIncomeBetween(
                portfolioId,
                monthStart,
                today);

        BigDecimal monthlyExpense = transactionRepository.totalExpenseBetween(
                portfolioId,
                monthStart,
                today);

        Transaction largestTransaction = transactions.stream()
                .max(
                        Comparator.comparing(
                                Transaction::getTotalAmount))
                .orElse(null);

        List<CategoryExpenseResponse> categoryExpenses = getExpenseByCategory(
                portfolioId,
                monthStart,
                today,
                userId);

        return new FinancialSummaryResponse(
                portfolioId,
                totalIncome,
                totalExpense,
                netCashFlow,
                totalBuy,
                totalSell,
                monthlyIncome,
                monthlyExpense,
                transactions.size(),
                largestTransaction != null
                        ? largestTransaction.getTotalAmount()
                        : BigDecimal.ZERO,
                categoryExpenses);
    }

    @Override
    public List<CategoryExpenseResponse> getExpenseByCategory(
            Long portfolioId,
            LocalDate startDate,
            LocalDate endDate,
            Long userId) {

        ownershipService.getOwnedPortfolio(
                portfolioId,
                userId);

        /*
         * Category is not currently mapped on Transaction.
         * Return an empty breakdown rather than generating
         * incorrect category information.
         */
        return new ArrayList<>();
    }
}