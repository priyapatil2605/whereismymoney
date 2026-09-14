package com.whereismymoney.backend.repository;

import com.whereismymoney.backend.entity.EntryType;
import com.whereismymoney.backend.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository
                extends JpaRepository<Transaction, Long> {

        List<Transaction> findByPortfolioIdOrderByTransactionDateDesc(
                        Long portfolioId);

        List<Transaction> findByPortfolioIdAndEntryTypeOrderByTransactionDateDesc(
                        Long portfolioId,
                        EntryType entryType);

        Optional<Transaction> findByIdAndPortfolioUserId(
                        Long transactionId,
                        Long userId);

        @Query("""
                        SELECT COALESCE(SUM(t.totalAmount), 0)
                        FROM Transaction t
                        WHERE t.portfolio.id = :portfolioId
                        AND t.transactionType = 'BUY'
                        """)
        BigDecimal totalBuyAmount(
                        @Param("portfolioId") Long portfolioId);

        @Query("""
                        SELECT COALESCE(SUM(t.totalAmount), 0)
                        FROM Transaction t
                        WHERE t.portfolio.id = :portfolioId
                        AND t.transactionType = 'SELL'
                        """)
        BigDecimal totalSellAmount(
                        @Param("portfolioId") Long portfolioId);

        @Query("""
                        SELECT COALESCE(SUM(t.totalAmount), 0)
                        FROM Transaction t
                        WHERE t.portfolio.id = :portfolioId
                        AND t.entryType = 'INCOME'
                        """)
        BigDecimal getTotalIncome(
                        @Param("portfolioId") Long portfolioId);

        @Query("""
                        SELECT COALESCE(SUM(t.totalAmount), 0)
                        FROM Transaction t
                        WHERE t.portfolio.id = :portfolioId
                        AND t.entryType = 'EXPENSE'
                        """)
        BigDecimal getTotalExpense(
                        @Param("portfolioId") Long portfolioId);

        @Query("""
                        SELECT COALESCE(SUM(t.totalAmount), 0)
                        FROM Transaction t
                        WHERE t.portfolio.id = :portfolioId
                        AND t.entryType = 'INCOME'
                        AND t.transactionDate BETWEEN :startDate AND :endDate
                        """)
        BigDecimal totalIncomeBetween(
                        @Param("portfolioId") Long portfolioId,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        @Query("""
                        SELECT COALESCE(SUM(t.totalAmount), 0)
                        FROM Transaction t
                        WHERE t.portfolio.id = :portfolioId
                        AND t.entryType = 'EXPENSE'
                        AND t.transactionDate BETWEEN :startDate AND :endDate
                        """)
        BigDecimal totalExpenseBetween(
                        @Param("portfolioId") Long portfolioId,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        @Query("""
                        SELECT t
                        FROM Transaction t
                        WHERE t.portfolio.id = :portfolioId
                        ORDER BY t.totalAmount DESC
                        """)
        List<Transaction> findLargestTransactions(
                        @Param("portfolioId") Long portfolioId);

        boolean existsByPortfolioIdAndTransactionDateAndTotalAmount(
                        Long portfolioId,
                        LocalDate transactionDate,
                        BigDecimal totalAmount);
}