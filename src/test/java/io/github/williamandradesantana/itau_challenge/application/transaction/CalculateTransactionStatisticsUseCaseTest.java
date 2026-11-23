package io.github.williamandradesantana.itau_challenge.application.transaction;

import io.github.williamandradesantana.itau_challenge.application.transaction.dtos.CalculateTransactionStatisticsOutput;
import io.github.williamandradesantana.itau_challenge.domain.transaction.TransactionRepository;
import io.github.williamandradesantana.itau_challenge.domain.transaction.TransactionStatistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalculateTransactionStatisticsUseCaseTest {

    @Mock
    private TransactionRepository transactionRepository;

    private Clock clock;

    private CalculateTransactionStatisticsUseCase calculateTransactionStatisticsUseCase;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2026-11-23T12:00:00Z"), ZoneOffset.UTC);
        calculateTransactionStatisticsUseCase = new CalculateTransactionStatisticsUseCase(
            transactionRepository,
            clock
        );
    }

    @Test
    @DisplayName("Test: should calculate statistics for the last 60 seconds")
    void test_ShouldCalculateStatisticsForTheLast60Seconds() {
        TransactionStatistics statistics = new TransactionStatistics(
            3L,
            BigDecimal.valueOf(423.45),
            BigDecimal.valueOf(141.15),
            BigDecimal.valueOf(50.00),
            BigDecimal.valueOf(250.00)
        );

        OffsetDateTime expectedStartedDateTime = OffsetDateTime.parse("2026-11-23T11:59:00Z");

        when(transactionRepository.calculateStatisticsSince(expectedStartedDateTime)).thenReturn(statistics);

        CalculateTransactionStatisticsOutput output = calculateTransactionStatisticsUseCase.execute();

        assertAll(
            () -> assertEquals(3L, statistics.count()),
            () -> assertEquals(BigDecimal.valueOf(423.45), statistics.sum()),
            () -> assertEquals(BigDecimal.valueOf(141.15), statistics.avg()),
            () -> assertEquals(BigDecimal.valueOf(50.00), statistics.min()),
            () -> assertEquals(BigDecimal.valueOf(250.00), statistics.max())
        );

        verify(transactionRepository, times(1)).calculateStatisticsSince(expectedStartedDateTime);
    }

    @Test
    @DisplayName("Test: should return zero statistics when there are no transactions")
    void test_ShouldReturnZeroStatisticsWhenThereAreNoTransactions() {

        TransactionStatistics statistics = new TransactionStatistics(
            0L,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO
        );

        when(transactionRepository.calculateStatisticsSince(any(OffsetDateTime.class)))
                .thenReturn(statistics);

        CalculateTransactionStatisticsOutput output = calculateTransactionStatisticsUseCase.execute();

        assertAll(
            () -> assertEquals(0L, output.count()),
            () -> assertEquals(BigDecimal.ZERO, output.sum()),
            () -> assertEquals(BigDecimal.ZERO, output.avg()),
            () -> assertEquals(BigDecimal.ZERO, output.min()),
            () -> assertEquals(BigDecimal.ZERO, output.max())
        );

        verify(transactionRepository, times(1))
                .calculateStatisticsSince(any(OffsetDateTime.class));
    }
}