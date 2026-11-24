package io.github.williamandradesantana.itau_challenge.infrastructure.persistence.transaction;

import io.github.williamandradesantana.itau_challenge.domain.transaction.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransactionMapperTest {

    private TransactionMapper transactionMapper;

    @BeforeEach
    void setUp() {
        transactionMapper = new TransactionMapper();
    }

    @Test
    @DisplayName("Test: entity to domain")
    void test_ConvertingEntityToDomain() {
        TransactionJpaEntity entity = new TransactionJpaEntity(
            UUID.randomUUID(), BigDecimal.valueOf(100.0), OffsetDateTime.parse("2026-11-23T10:30:10-03:00")
        );

        Transaction transaction = transactionMapper.toDomain(entity);

        assertEquals(transaction.getId(), entity.getId(), () -> "The id not matches");
        assertEquals(transaction.getAmount(), entity.getAmount(), () -> "The amount not matches");
        assertEquals(transaction.getDateTime(), entity.getDateTime(), () -> "The datetime not matches");
    }

    @Test
    @DisplayName("Test: domain to entity")
    void test_ConvertingDomainToEntity() {
        Transaction transaction = Transaction.create(
            BigDecimal.valueOf(100.0), OffsetDateTime.parse("2026-11-23T10:30:10-03:00")
        );

        TransactionJpaEntity entity = transactionMapper.toJpaEntity(transaction);

        assertEquals(entity.getId(), transaction.getId(), () -> "The id not matches");
        assertEquals(entity.getAmount(), transaction.getAmount(), () -> "The amount not matches");
        assertEquals(entity.getDateTime(), transaction.getDateTime(), () -> "The datetime not matches");
    }
}