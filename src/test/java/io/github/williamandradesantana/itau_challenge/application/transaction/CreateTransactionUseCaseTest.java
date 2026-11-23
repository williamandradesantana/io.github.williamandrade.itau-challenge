package io.github.williamandradesantana.itau_challenge.application.transaction;

import io.github.williamandradesantana.itau_challenge.application.shared.exceptions.RequiredObjectIsNullException;
import io.github.williamandradesantana.itau_challenge.application.transaction.dtos.CreateTransactionCommand;
import io.github.williamandradesantana.itau_challenge.domain.transaction.Transaction;
import io.github.williamandradesantana.itau_challenge.domain.transaction.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateTransactionUseCaseTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private CreateTransactionUseCase createTransactionUseCase;

    private CreateTransactionCommand command;

    @BeforeEach
    void setUp() {
        command = new CreateTransactionCommand(
            BigDecimal.valueOf(100.00), OffsetDateTime.parse("2026-11-23T10:30:10-03:00")
        );
        createTransactionUseCase = new CreateTransactionUseCase(transactionRepository);
    }

    @Test
    @DisplayName("Test: should create and save transaction when command is valid")
    void test_ShouldCreateAndSaveTransactionWhenCommandIsValid() {
        createTransactionUseCase.execute(command);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

        verify(transactionRepository, times(1)).save(captor.capture());

        Transaction transaction = captor.getValue();

        assertNotNull(transaction.getId(), () -> "The id cannot be null");
        assertEquals(command.amount(), transaction.getAmount(), () -> "The amount not matches");
        assertEquals(command.datetime(), transaction.getDateTime(), () -> "The datetime not matches");
    }

    @Test
    @DisplayName("Test: should throw exception when command is null")
    void test_ShouldThrowExceptionWhenCommandIsNull() {
        String expectedMessage = "It is not allowed to persist a null object!";
        RequiredObjectIsNullException exception = assertThrows(
            RequiredObjectIsNullException.class, () -> createTransactionUseCase.execute(null)
        );

        assertEquals(expectedMessage, exception.getMessage(), () -> "The message not matches!");
        verifyNoInteractions(transactionRepository);
    }
}