package io.github.williamandradesantana.itau_challenge.application.transaction;

import io.github.williamandradesantana.itau_challenge.application.transaction.dtos.CalculateTransactionStatisticsOutput;
import io.github.williamandradesantana.itau_challenge.domain.transaction.TransactionRepository;
import io.github.williamandradesantana.itau_challenge.domain.transaction.TransactionStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.OffsetDateTime;

public class CalculateTransactionStatisticsUseCase {

    private static final Logger log = LoggerFactory.getLogger(CalculateTransactionStatisticsUseCase.class);

    private final TransactionRepository transactionRepository;
    private final Clock clock;

    public CalculateTransactionStatisticsUseCase(TransactionRepository transactionRepository, Clock clock) {
        this.transactionRepository = transactionRepository;
        this.clock = clock;
    }

    public CalculateTransactionStatisticsOutput execute() {
        OffsetDateTime startDatetime = OffsetDateTime.now(clock).minusSeconds(60);

        TransactionStatistics statistics = transactionRepository.calculateStatisticsSince(startDatetime);

        log.info("Statistics for the last 60 seconds");
        return new CalculateTransactionStatisticsOutput(
            statistics.count(),
            statistics.sum(),
            statistics.avg(),
            statistics.min(),
            statistics.max()
        );
    }
}
