package com.whereismymoney.backend.service;

import com.whereismymoney.backend.entity.Transaction;
import com.whereismymoney.backend.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private OwnershipService ownershipService;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Test
    void userCannotAccessAnotherUsersTransaction() {

        when(transactionRepository
                .findByIdAndPortfolioUserId(1L, 12L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> transactionService
                        .getTransactionById(1L, 12L));

        assertEquals(
                403,
                exception.getStatusCode().value());

        verify(transactionRepository)
                .findByIdAndPortfolioUserId(1L, 12L);
    }

    @Test
    void userCannotDeleteAnotherUsersTransaction() {

        when(transactionRepository
                .findByIdAndPortfolioUserId(1L, 12L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> transactionService
                        .deleteTransaction(1L, 12L));

        assertEquals(
                403,
                exception.getStatusCode().value());

        verify(transactionRepository, never())
                .delete(any(Transaction.class));
    }

    @Test
    void userCannotUpdateAnotherUsersTransaction() {

        when(transactionRepository
                .findByIdAndPortfolioUserId(1L, 12L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> transactionService.updateTransaction(
                        1L,
                        new Transaction(),
                        12L));

        assertEquals(
                403,
                exception.getStatusCode().value());

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }

    @Test
    void transactionCreationRequiresPortfolio() {

        Transaction transaction = new Transaction();

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> transactionService.createTransaction(
                        transaction,
                        11L));

        assertEquals(
                400,
                exception.getStatusCode().value());

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }
}