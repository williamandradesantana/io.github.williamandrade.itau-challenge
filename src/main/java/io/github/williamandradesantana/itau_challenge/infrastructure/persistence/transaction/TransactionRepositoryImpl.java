package io.github.williamandradesantana.itau_challenge.infrastructure.persistence.transaction;

import io.github.williamandradesantana.itau_challenge.domain.transaction.Transaction;
import io.github.williamandradesantana.itau_challenge.domain.transaction.TransactionRepository;
import io.github.williamandradesantana.itau_challenge.domain.transaction.TransactionStatistics;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public class TransactionRepositoryImpl implements TransactionRepository {

    private final TransactionJpaRepository jpaRepository;
    private final TransactionMapper mapper;

    public TransactionRepositoryImpl(TransactionJpaRepository jpaRepository, TransactionMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public TransactionStatistics calculateStatisticsSince(OffsetDateTime startDateTime) {
        TransactionStatisticsProjection stats = jpaRepository.calculateStatisticsSince(startDateTime);
        return new TransactionStatistics(stats.getCount(), stats.getSum(), stats.getAvg(), stats.getMin(), stats.getMax());

    }

    @Override
    public Optional<Transaction> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public void save(Transaction transaction) {
        jpaRepository.save(mapper.toJpaEntity(transaction));
    }

    @Override
    public void delete(UUID id) {
        jpaRepository.deleteById(id);
    }
}
