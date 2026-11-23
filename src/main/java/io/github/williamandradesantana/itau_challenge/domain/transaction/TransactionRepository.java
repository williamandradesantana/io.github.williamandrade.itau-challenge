package io.github.williamandradesantana.itau_challenge.domain.transaction;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository {
    TransactionStatistics calculateStatisticsSince(OffsetDateTime startDateTime);
    Optional<Transaction> findById(UUID id);
    void save(Transaction transaction);
    void delete(UUID id);
}
