package io.github.williamandradesantana.itau_challenge.application.transaction;

import io.github.williamandradesantana.itau_challenge.application.shared.exceptions.RequiredObjectIsNullException;
import io.github.williamandradesantana.itau_challenge.application.shared.exceptions.ResourceNotFoundException;
import io.github.williamandradesantana.itau_challenge.domain.transaction.Transaction;
import io.github.williamandradesantana.itau_challenge.domain.transaction.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteTransactionUseCaseTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private DeleteTransactionUseCase deleteTransactionUseCase;

    @BeforeEach
    void setUp() {
        deleteTransactionUseCase = new DeleteTransactionUseCase(transactionRepository);
    }

    @Test
    @DisplayName("Test: when the transaction ID is valid, the transaction should be deleted.")
    void test_WhenTheTransactionIdIsValid_TheTransactionShouldBeDeleted() {
        Transaction transaction = Transaction.create(
            BigDecimal.valueOf(100.00), OffsetDateTime.parse("2026-11-23T10:30:10-03:00")
        );

        when(transactionRepository.findById(transaction.getId())).thenReturn(Optional.of(transaction));
        deleteTransactionUseCase.execute(transaction.getId());

        verify(transactionRepository, times(1)).findById(transaction.getId());
        verify(transactionRepository, times(1)).delete(transaction.getId());
    }

    @Test
    @DisplayName("Test: should throw exception when transaction does not exist")
    void test_ShouldThrowException_WhenTransactionDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(transactionRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> deleteTransactionUseCase.execute(id));

        verify(transactionRepository, times(1)).findById(id);
        verify(transactionRepository, never()).delete(any(UUID.class));
    }

    @Test
    @DisplayName("Test: should throw exception when ID is null")
    void test_ShouldThrowExceptionWhenIdIsNull() {
        assertThrows(RequiredObjectIsNullException.class, () -> deleteTransactionUseCase.execute(null));
        verifyNoInteractions(transactionRepository);
    }
}