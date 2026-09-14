package com.whereismymoney.backend.service;

import com.whereismymoney.backend.entity.EntryType;
import com.whereismymoney.backend.entity.Portfolio;
import com.whereismymoney.backend.entity.Transaction;
import com.whereismymoney.backend.entity.Category;
import com.whereismymoney.backend.repository.CategoryRepository;
import com.whereismymoney.backend.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TransactionServiceImpl
                implements TransactionService {

        private static final Logger logger = LoggerFactory.getLogger(
                        TransactionServiceImpl.class);

        private final TransactionRepository transactionRepository;
        private final CategoryRepository categoryRepository;
        private final OwnershipService ownershipService;

        public TransactionServiceImpl(
                        TransactionRepository transactionRepository,
                        CategoryRepository categoryRepository,
                        OwnershipService ownershipService) {

                this.transactionRepository = transactionRepository;

                this.categoryRepository = categoryRepository;

                this.ownershipService = ownershipService;
        }

        @Override
        public Transaction createTransaction(
                        Transaction transaction,
                        Long userId) {

                if (transaction.getPortfolio() == null ||
                                transaction.getPortfolio().getId() == null) {

                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Portfolio is required");
                }

                if (transaction.getTotalAmount() == null ||
                                transaction.getTotalAmount()
                                                .compareTo(BigDecimal.ZERO) <= 0) {

                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Transaction amount must be positive");
                }

                Long portfolioId = transaction.getPortfolio().getId();

                Portfolio ownedPortfolio = ownershipService.getOwnedPortfolio(
                                portfolioId,
                                userId);

                transaction.setPortfolio(
                                ownedPortfolio);

                if (transaction.getCategory() != null &&
                                transaction.getCategory().getId() != null) {

                        Category category = categoryRepository.findById(
                                        transaction
                                                        .getCategory()
                                                        .getId())
                                        .orElseThrow(() -> new ResponseStatusException(
                                                        HttpStatus.BAD_REQUEST,
                                                        "Category does not exist"));

                        transaction.setCategory(category);
                }

                Transaction saved = transactionRepository.save(
                                transaction);

                logger.info(
                                "Transaction created: transactionId={}, userId={}, portfolioId={}",
                                saved.getId(),
                                userId,
                                portfolioId);

                return saved;
        }

        @Override
        public List<Transaction> getTransactionsByPortfolio(
                        Long portfolioId,
                        Long userId) {

                ownershipService.getOwnedPortfolio(
                                portfolioId,
                                userId);

                return transactionRepository
                                .findByPortfolioIdOrderByTransactionDateDesc(
                                                portfolioId);
        }

        @Override
        public List<Transaction> getTransactionsByType(
                        Long portfolioId,
                        EntryType entryType,
                        Long userId) {

                ownershipService.getOwnedPortfolio(
                                portfolioId,
                                userId);

                return transactionRepository
                                .findByPortfolioIdAndEntryTypeOrderByTransactionDateDesc(
                                                portfolioId,
                                                entryType);
        }

        @Override
        public Transaction getTransactionById(
                        Long transactionId,
                        Long userId) {

                return transactionRepository
                                .findByIdAndPortfolioUserId(
                                                transactionId,
                                                userId)
                                .orElseThrow(() -> {

                                        logger.warn(
                                                        "Unauthorized transaction access: transactionId={}, userId={}",
                                                        transactionId,
                                                        userId);

                                        return new ResponseStatusException(
                                                        HttpStatus.FORBIDDEN,
                                                        "You do not have access to this transaction");
                                });
        }

        @Override
        public Transaction updateTransaction(
                        Long transactionId,
                        Transaction transaction,
                        Long userId) {

                Transaction existing = transactionRepository
                                .findByIdAndPortfolioUserId(
                                                transactionId,
                                                userId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.FORBIDDEN,
                                                "You do not have access to this transaction"));

                if (transaction.getTotalAmount() == null ||
                                transaction.getTotalAmount()
                                                .compareTo(BigDecimal.ZERO) <= 0) {

                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Transaction amount must be positive");
                }

                existing.setAsset(
                                transaction.getAsset());

                existing.setTransactionType(
                                transaction.getTransactionType());

                existing.setEntryType(
                                transaction.getEntryType());

                existing.setQuantity(
                                transaction.getQuantity());

                existing.setPricePerUnit(
                                transaction.getPricePerUnit());

                existing.setTransactionDate(
                                transaction.getTransactionDate());

                existing.setTotalAmount(
                                transaction.getTotalAmount());

                if (transaction.getCategory() != null &&
                                transaction.getCategory().getId() != null) {

                        Category category = categoryRepository.findById(
                                        transaction
                                                        .getCategory()
                                                        .getId())
                                        .orElseThrow(() -> new ResponseStatusException(
                                                        HttpStatus.BAD_REQUEST,
                                                        "Category does not exist"));

                        existing.setCategory(category);
                }

                return transactionRepository.save(
                                existing);
        }

        @Override
        public void deleteTransaction(
                        Long transactionId,
                        Long userId) {

                Transaction transaction = transactionRepository
                                .findByIdAndPortfolioUserId(
                                                transactionId,
                                                userId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.FORBIDDEN,
                                                "You do not have access to this transaction"));

                transactionRepository.delete(
                                transaction);

                logger.info(
                                "Transaction deleted: transactionId={}, userId={}",
                                transactionId,
                                userId);
        }
}