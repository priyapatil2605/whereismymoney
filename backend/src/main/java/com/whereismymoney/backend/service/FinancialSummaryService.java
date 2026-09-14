package com.whereismymoney.backend.service;

import com.whereismymoney.backend.dto.CategoryExpenseResponse;
import com.whereismymoney.backend.dto.FinancialSummaryResponse;

import java.time.LocalDate;
import java.util.List;

public interface FinancialSummaryService {

    FinancialSummaryResponse getSummary(
            Long portfolioId,
            Long userId);

    List<CategoryExpenseResponse> getExpenseByCategory(
            Long portfolioId,
            LocalDate startDate,
            LocalDate endDate,
            Long userId);
}