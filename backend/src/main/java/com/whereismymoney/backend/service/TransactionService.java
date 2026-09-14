package com.whereismymoney.backend.service;

import com.whereismymoney.backend.entity.EntryType;
import com.whereismymoney.backend.entity.Transaction;

import java.util.List;

public interface TransactionService {

        Transaction createTransaction(
                        Transaction transaction,
                        Long userId);

        List<Transaction> getTransactionsByPortfolio(
                        Long portfolioId,
                        Long userId);

        List<Transaction> getTransactionsByType(
                        Long portfolioId,
                        EntryType entryType,
                        Long userId);

        Transaction getTransactionById(
                        Long transactionId,
                        Long userId);

        Transaction updateTransaction(
                        Long transactionId,
                        Transaction transaction,
                        Long userId);

        void deleteTransaction(
                        Long transactionId,
                        Long userId);
}