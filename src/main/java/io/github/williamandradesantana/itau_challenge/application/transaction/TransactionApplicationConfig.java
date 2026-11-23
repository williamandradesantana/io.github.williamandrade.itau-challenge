package io.github.williamandradesantana.itau_challenge.application.transaction;

import io.github.williamandradesantana.itau_challenge.domain.transaction.TransactionRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class TransactionApplicationConfig {

    @Bean
    public CreateTransactionUseCase createTransactionUseCase(TransactionRepository transactionRepository) {
        return new CreateTransactionUseCase(transactionRepository);
    }

    @Bean
    public DeleteTransactionUseCase deleteTransactionUseCase(TransactionRepository transactionRepository) {
        return new DeleteTransactionUseCase(transactionRepository);
    }

    @Bean
    public CalculateTransactionStatisticsUseCase calculateTransactionStatisticsUseCase(
            TransactionRepository transactionRepository, Clock clock
    ) {
        return new CalculateTransactionStatisticsUseCase(transactionRepository, clock);
    }
}
