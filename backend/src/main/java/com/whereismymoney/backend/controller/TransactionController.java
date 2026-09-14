package com.whereismymoney.backend.controller;

import com.whereismymoney.backend.entity.EntryType;
import com.whereismymoney.backend.entity.Transaction;
import com.whereismymoney.backend.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

        private final TransactionService transactionService;

        public TransactionController(
                        TransactionService transactionService) {

                this.transactionService = transactionService;
        }

        @PostMapping
        public ResponseEntity<Transaction> createTransaction(
                        @RequestBody Transaction transaction,
                        Authentication authentication) {

                Long userId = getUserId(authentication);

                Transaction created = transactionService.createTransaction(
                                transaction,
                                userId);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(created);
        }

        @GetMapping("/portfolio/{portfolioId}")
        public ResponseEntity<List<Transaction>> getByPortfolio(
                        @PathVariable Long portfolioId,
                        Authentication authentication) {

                Long userId = getUserId(authentication);

                return ResponseEntity.ok(
                                transactionService.getTransactionsByPortfolio(
                                                portfolioId,
                                                userId));
        }

        @GetMapping("/portfolio/{portfolioId}/type/{entryType}")
        public ResponseEntity<List<Transaction>> getByType(
                        @PathVariable Long portfolioId,
                        @PathVariable EntryType entryType,
                        Authentication authentication) {

                Long userId = getUserId(authentication);

                return ResponseEntity.ok(
                                transactionService.getTransactionsByType(
                                                portfolioId,
                                                entryType,
                                                userId));
        }

        @GetMapping("/{transactionId}")
        public ResponseEntity<Transaction> getById(
                        @PathVariable Long transactionId,
                        Authentication authentication) {

                Long userId = getUserId(authentication);

                return ResponseEntity.ok(
                                transactionService.getTransactionById(
                                                transactionId,
                                                userId));
        }

        @PutMapping("/{transactionId}")
        public ResponseEntity<Transaction> update(
                        @PathVariable Long transactionId,
                        @RequestBody Transaction transaction,
                        Authentication authentication) {

                Long userId = getUserId(authentication);

                return ResponseEntity.ok(
                                transactionService.updateTransaction(
                                                transactionId,
                                                transaction,
                                                userId));
        }

        @DeleteMapping("/{transactionId}")
        public ResponseEntity<Void> delete(
                        @PathVariable Long transactionId,
                        Authentication authentication) {

                Long userId = getUserId(authentication);

                transactionService.deleteTransaction(
                                transactionId,
                                userId);

                return ResponseEntity.noContent().build();
        }

        private Long getUserId(Authentication authentication) {

                return (Long) authentication.getPrincipal();
        }
}