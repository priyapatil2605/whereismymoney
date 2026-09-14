package com.whereismymoney.backend.service;

import com.whereismymoney.backend.entity.EntryType;
import com.whereismymoney.backend.entity.Transaction;
import com.whereismymoney.backend.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;

@Service
public class DeterministicInsightService
        implements FinancialInsightService {

    private final TransactionRepository transactionRepository;

    public DeterministicInsightService(
            TransactionRepository transactionRepository) {

        this.transactionRepository = transactionRepository;
    }

    @Override
    public String answerQuestion(
            Long portfolioId,
            String question) {

        List<Transaction> transactions = transactionRepository
                .findByPortfolioIdOrderByTransactionDateDesc(
                        portfolioId);

        if (transactions.isEmpty()) {
            return "There are no transactions recorded in this portfolio yet. Add some transactions and I can analyze your financial activity.";
        }

        String normalizedQuestion = question == null
                ? ""
                : question.toLowerCase();

        BigDecimal totalVolume = transactions.stream()
                .map(Transaction::getTotalAmount)
                .filter(amount -> amount != null)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add);

        BigDecimal totalExpenses = transactions.stream()
                .filter(t -> t.getEntryType() == EntryType.EXPENSE)
                .map(Transaction::getTotalAmount)
                .filter(amount -> amount != null)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add);

        BigDecimal totalIncome = transactions.stream()
                .filter(t -> t.getEntryType() == EntryType.INCOME)
                .map(Transaction::getTotalAmount)
                .filter(amount -> amount != null)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add);

        BigDecimal buyAmount = transactionRepository.totalBuyAmount(portfolioId);

        BigDecimal sellAmount = transactionRepository.totalSellAmount(portfolioId);

        if (buyAmount == null) {
            buyAmount = BigDecimal.ZERO;
        }

        if (sellAmount == null) {
            sellAmount = BigDecimal.ZERO;
        }

        Transaction largestTransaction = transactions.stream()
                .filter(t -> t.getTotalAmount() != null)
                .max(
                        Comparator.comparing(
                                Transaction::getTotalAmount))
                .orElse(null);

        YearMonth currentMonth = YearMonth.now();

        List<Transaction> currentMonthTransactions = transactions.stream()
                .filter(t -> t.getTransactionDate() != null &&
                        YearMonth.from(
                                t.getTransactionDate()).equals(currentMonth))
                .toList();

        BigDecimal currentMonthVolume = currentMonthTransactions.stream()
                .map(Transaction::getTotalAmount)
                .filter(amount -> amount != null)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add);

        if (normalizedQuestion.contains("month") ||
                normalizedQuestion.contains("money this")) {

            return buildMonthlyAnswer(
                    currentMonth,
                    currentMonthTransactions,
                    currentMonthVolume,
                    totalVolume);
        }

        if (normalizedQuestion.contains("biggest") ||
                normalizedQuestion.contains("largest")) {

            return buildLargestTransactionAnswer(
                    largestTransaction,
                    transactions);
        }

        if (normalizedQuestion.contains("portfolio") ||
                normalizedQuestion.contains("performing") ||
                normalizedQuestion.contains("performance")) {

            return buildPortfolioAnswer(
                    transactions,
                    totalVolume,
                    totalIncome,
                    totalExpenses,
                    buyAmount,
                    sellAmount);
        }

        return buildGeneralAnswer(
                transactions,
                totalVolume,
                totalIncome,
                totalExpenses,
                buyAmount,
                sellAmount,
                largestTransaction);
    }

    private String buildMonthlyAnswer(
            YearMonth month,
            List<Transaction> monthTransactions,
            BigDecimal monthVolume,
            BigDecimal totalVolume) {

        if (monthTransactions.isEmpty()) {
            return "There are no transactions recorded for "
                    + month.getMonth()
                    + " "
                    + month.getYear()
                    + ". Your portfolio currently has "
                    + totalVolume.setScale(
                            2,
                            RoundingMode.HALF_UP)
                    + " of recorded transaction volume overall.";
        }

        BigDecimal monthExpenses = monthTransactions.stream()
                .filter(t -> t.getEntryType() == EntryType.EXPENSE)
                .map(Transaction::getTotalAmount)
                .filter(amount -> amount != null)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add);

        BigDecimal monthIncome = monthTransactions.stream()
                .filter(t -> t.getEntryType() == EntryType.INCOME)
                .map(Transaction::getTotalAmount)
                .filter(amount -> amount != null)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add);

        return "For "
                + month.getMonth()
                + " "
                + month.getYear()
                + ", you recorded "
                + monthTransactions.size()
                + " transaction"
                + (monthTransactions.size() == 1 ? "" : "s")
                + " with total activity of ₹"
                + format(monthVolume)
                + ". "
                + "Recorded expenses were ₹"
                + format(monthExpenses)
                + " and recorded income was ₹"
                + format(monthIncome)
                + ". "
                + "This analysis is based directly on the transactions stored in your portfolio.";
    }

    private String buildLargestTransactionAnswer(
            Transaction largestTransaction,
            List<Transaction> transactions) {

        if (largestTransaction == null) {
            return "I could not find a transaction with a recorded amount.";
        }

        BigDecimal total = transactions.stream()
                .map(Transaction::getTotalAmount)
                .filter(amount -> amount != null)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add);

        BigDecimal amount = largestTransaction.getTotalAmount();

        BigDecimal percentage = BigDecimal.ZERO;

        if (total.compareTo(BigDecimal.ZERO) > 0) {
            percentage = amount
                    .multiply(BigDecimal.valueOf(100))
                    .divide(
                            total,
                            2,
                            RoundingMode.HALF_UP);
        }

        String type = largestTransaction.getTransactionType() != null
                ? largestTransaction
                        .getTransactionType()
                        .toString()
                : "UNKNOWN";

        return "Your largest recorded transaction is ₹"
                + format(amount)
                + ". It is a "
                + type
                + " transaction and represents approximately "
                + percentage
                + "% of your total recorded transaction volume. "
                + "There are "
                + transactions.size()
                + " transactions in this portfolio.";
    }

    private String buildPortfolioAnswer(
            List<Transaction> transactions,
            BigDecimal totalVolume,
            BigDecimal totalIncome,
            BigDecimal totalExpenses,
            BigDecimal buyAmount,
            BigDecimal sellAmount) {

        BigDecimal netCashFlow = totalIncome.subtract(totalExpenses);

        return "Based on your recorded portfolio activity, you have "
                + transactions.size()
                + " transactions with total transaction volume of ₹"
                + format(totalVolume)
                + ". "
                + "Recorded income is ₹"
                + format(totalIncome)
                + " and recorded expenses are ₹"
                + format(totalExpenses)
                + ", giving a recorded net cash flow of ₹"
                + format(netCashFlow)
                + ". "
                + "Investment purchases total ₹"
                + format(buyAmount)
                + " and investment sales total ₹"
                + format(sellAmount)
                + ". "
                + "These figures describe recorded activity; they are not a prediction or investment recommendation.";
    }

    private String buildGeneralAnswer(
            List<Transaction> transactions,
            BigDecimal totalVolume,
            BigDecimal totalIncome,
            BigDecimal totalExpenses,
            BigDecimal buyAmount,
            BigDecimal sellAmount,
            Transaction largestTransaction) {

        String largest = largestTransaction != null
                ? " Your largest recorded transaction was ₹"
                        + format(
                                largestTransaction.getTotalAmount())
                        + "."
                : "";

        return "I analyzed "
                + transactions.size()
                + " recorded transactions in this portfolio. "
                + "Total transaction volume is ₹"
                + format(totalVolume)
                + ". "
                + "Recorded income is ₹"
                + format(totalIncome)
                + " and expenses are ₹"
                + format(totalExpenses)
                + ". "
                + "Investment purchases total ₹"
                + format(buyAmount)
                + " while investment sales total ₹"
                + format(sellAmount)
                + "."
                + largest
                + " Ask about your monthly activity, largest transactions, or portfolio performance for a more focused breakdown.";
    }

    private String format(BigDecimal value) {

        if (value == null) {
            return "0.00";
        }

        return value
                .setScale(2, RoundingMode.HALF_UP)
                .toPlainString();
    }
}