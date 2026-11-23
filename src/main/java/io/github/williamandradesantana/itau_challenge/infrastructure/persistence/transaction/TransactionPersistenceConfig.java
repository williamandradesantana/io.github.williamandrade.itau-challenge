package io.github.williamandradesantana.itau_challenge.infrastructure.persistence.transaction;

import io.github.williamandradesantana.itau_challenge.domain.transaction.TransactionRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TransactionPersistenceConfig {

    @Bean
    public TransactionMapper transactionMapper() {
        return new TransactionMapper();
    }

    @Bean
    public TransactionRepository transactionRepository(
            TransactionJpaRepository jpaRepository, TransactionMapper mapper
    ) {
        return new TransactionRepositoryImpl(jpaRepository, mapper);
    }
}
