package io.github.williamandradesantana.itau_challenge.domain.transaction;

import io.github.williamandradesantana.itau_challenge.domain.transaction.exceptions.InvalidTransactionAmountException;
import io.github.williamandradesantana.itau_challenge.domain.transaction.exceptions.InvalidTransactionDateTimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    private Transaction transaction;

    @BeforeEach
    void setUp() {
        transaction = Transaction.create(
            BigDecimal.valueOf(100.00), OffsetDateTime.parse("2026-11-23T10:30:10-03:00")
        );
    }

    @Test
    @DisplayName("Test: when the transaction is valid, should return all fields correctly")
    void test_WhenTheTransactionIsValid_ShouldReturnAllFieldsCorrectly() {
        BigDecimal expectedAmount = BigDecimal.valueOf(100.00);
        OffsetDateTime expectedDateTime = OffsetDateTime.parse("2026-11-23T10:30:10-03:00");

        assertNotNull(transaction.getId(), () -> "The id cannot be null");
        assertEquals(expectedAmount, transaction.getAmount(), () -> "The amount not matches");
        assertEquals(expectedDateTime, transaction.getDateTime(), () -> "The dateTime not matches");
    }

    @Test
    @DisplayName("Test: when the amount is zero, should return success")
    void test_WhenTheAmountIsZero_ShouldReturnSuccess() {
        assertDoesNotThrow(
            () -> transaction.setAmount(BigDecimal.ZERO),
            () -> "Creating transaction with zero amount should not throw an exception"
        );
    }

    @Test
    @DisplayName("Test: when the amount is null, should return exception")
    void test_WhenTheAmountIsNull_ShouldReturnException() {
        String expectedMessage = "The value cannot be less than zero or null";

        InvalidTransactionAmountException exception = assertThrows(
            InvalidTransactionAmountException.class,
            () -> transaction.setAmount(null)
        );

        assertEquals(expectedMessage, exception.getMessage(), () -> "The message not matches");
    }

    @Test
    @DisplayName("Test: when the amount is less than zero, should return exception")
    void test_WhenTheAmountIsLessThanZero_ShouldReturnException() {
        String expectedMessage = "The value cannot be less than zero or null";

        InvalidTransactionAmountException exception = assertThrows(
            InvalidTransactionAmountException.class,
            () -> transaction.setAmount(BigDecimal.valueOf(-1))
        );

        assertEquals(expectedMessage, exception.getMessage(), () -> "The message not matches");
    }

    @Test
    @DisplayName("Test: when the datetime is null, should return exception")
    void test_WhenTheDatetimeIsNull_ShouldReturnException() {
        String expectedMessage = "The datetime cannot be null!";

        InvalidTransactionDateTimeException exception = assertThrows(
            InvalidTransactionDateTimeException.class,
            () -> transaction.setDateTime(null)
        );

        assertEquals(expectedMessage, exception.getMessage(), () -> "The message not matches");
    }

    @Test
    @DisplayName("Test: when the datetime is in the future, should return exception")
    void test_WhenTheDatetimeIsInTheFuture_ShouldReturnException() {
        String expectedMessage = "The datetime cannot be in the future";

        InvalidTransactionDateTimeException exception = assertThrows(
            InvalidTransactionDateTimeException.class,
            () -> transaction.setDateTime(OffsetDateTime.now().plusDays(1))
        );

        assertEquals(expectedMessage, exception.getMessage(), () -> "The message not matches");
    }
}