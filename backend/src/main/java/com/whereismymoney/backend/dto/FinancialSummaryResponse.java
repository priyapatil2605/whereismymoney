package com.whereismymoney.backend.dto;

import java.math.BigDecimal;
import java.util.List;

public record FinancialSummaryResponse(

        Long portfolioId,

        BigDecimal totalIncome,

        BigDecimal totalExpense,

        BigDecimal netCashFlow,

        BigDecimal totalBuy,

        BigDecimal totalSell,

        BigDecimal monthlyIncome,

        BigDecimal monthlyExpense,

        int transactionCount,

        BigDecimal largestTransaction,

        List<CategoryExpenseResponse> categoryExpenses) {
}