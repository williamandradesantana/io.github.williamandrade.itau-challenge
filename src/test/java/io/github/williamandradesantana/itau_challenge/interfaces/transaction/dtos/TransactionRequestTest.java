package io.github.williamandradesantana.itau_challenge.interfaces.transaction.dtos;

import io.github.williamandradesantana.itau_challenge.application.transaction.dtos.CreateTransactionCommand;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TransactionRequestTest {

    private ValidatorFactory validatorFactory;
    private Validator validator;

    @BeforeEach
    void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterEach
    void tearDown() {
        validatorFactory.close();
    }

    @Test
    @DisplayName("Test: should create request when all fields are valid")
    void test_ShouldCreateRequestWhenFieldsAreValid() {
        TransactionRequest request = new TransactionRequest(
            new BigDecimal("100.00"),
            OffsetDateTime.parse("2026-11-23T10:30:10-03:00")
        );

        Set<ConstraintViolation<TransactionRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Test: should reject request when amount is null")
    void test_ShouldRejectRequestWhenAmountIsNull() {
        TransactionRequest request = new TransactionRequest(
                null,
                OffsetDateTime.parse("2026-11-23T10:30:10-03:00")
        );

        Set<ConstraintViolation<TransactionRequest>> violations = validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals("amount", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    @DisplayName("Test: should reject request when datetime is null")
    void test_ShouldRejectRequestWhenDatetimeIsNull() {
        TransactionRequest request = new TransactionRequest(new BigDecimal("100.00"), null);

        Set<ConstraintViolation<TransactionRequest>> violations = validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals("dateTime", violations.iterator().next().getPropertyPath().toString());
    }



    @Test
    @DisplayName("Test: should convert command into transaction request")
    void test_ShouldConvertCommandIntoTransactionRequest() {
        CreateTransactionCommand command = new CreateTransactionCommand(
            new BigDecimal("100.00"),
            OffsetDateTime.parse("2026-11-23T10:30:10-03:00")
        );
        TransactionRequest request = TransactionRequest.from(command);

        assertEquals(command.amount(), request.amount());
        assertEquals(command.datetime(), request.dateTime());
    }
}