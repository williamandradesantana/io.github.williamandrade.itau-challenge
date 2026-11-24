package io.github.williamandradesantana.itau_challenge.infrastructure.persistence.transaction;

import io.github.williamandradesantana.itau_challenge.domain.transaction.Transaction;
import io.github.williamandradesantana.itau_challenge.domain.transaction.TransactionRepository;
import io.github.williamandradesantana.itau_challenge.domain.transaction.TransactionStatistics;
import io.github.williamandradesantana.itau_challenge.infrastructure.persistence.PostgresIntegrationTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(TransactionPersistenceConfig.class)
class TransactionRepositoryImplTest extends PostgresIntegrationTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private EntityManager entityManager;

    private Transaction transaction;

    @BeforeEach
    void setUp() {
        transaction = Transaction.create(
            BigDecimal.valueOf(100.00), OffsetDateTime.parse("2026-11-23T10:30:10-03:00")
        );
    }

    @Test
    @DisplayName("Test: saving a transaction and return values")
    void test_SavingATransactionAndReturnValues() {
        transactionRepository.save(transaction);
        entityManager.flush();
        entityManager.clear();

        Optional<Transaction> result = transactionRepository.findById(transaction.getId());

        assertTrue(result.isPresent());

        Transaction savedTransaction = result.get();

        assertAll(
            () -> assertEquals(transaction.getId(), savedTransaction.getId()),
            () -> assertEquals(0, transaction.getAmount().compareTo(savedTransaction.getAmount())),
            () -> assertEquals(transaction.getDateTime().toInstant(), savedTransaction.getDateTime().toInstant())
        );
    }

    @Test
    @DisplayName("Test: should delete transaction successfully")
    void test_ShouldDeleteTransactionSuccessfully() {

        transactionRepository.save(transaction);

        transactionRepository.delete(transaction.getId());

        entityManager.flush();
        entityManager.clear();

        Optional<Transaction> result = transactionRepository.findById(transaction.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test: should calculate transaction statistics correctly")
    void test_ShouldCalculateTransactionStatisticsCorrectly() {

        OffsetDateTime now = OffsetDateTime.now(Clock.systemUTC());

        Transaction first = Transaction.create(BigDecimal.valueOf(100.00), now.minusSeconds(10));

        Transaction second = Transaction.create(BigDecimal.valueOf(200.00), now.minusSeconds(20));

        Transaction third = Transaction.create(BigDecimal.valueOf(50.00), now.minusSeconds(30));

        transactionRepository.save(first);
        transactionRepository.save(second);
        transactionRepository.save(third);

        TransactionStatistics statistics = transactionRepository.calculateStatisticsSince(now.minusSeconds(60));

        System.out.println(statistics);
        assertAll(
            () -> assertEquals(3L, statistics.count()),
            () -> assertEquals(0, new BigDecimal("350.00").compareTo(statistics.sum())),
                () -> assertEquals(0, new BigDecimal("116.67")
                        .compareTo(statistics.avg().setScale(2, RoundingMode.HALF_UP))),
            () -> assertEquals(0, new BigDecimal("50.00").compareTo(statistics.min())),
            () -> assertEquals(0, new BigDecimal("200.00").compareTo(statistics.max()))
        );
    }
}