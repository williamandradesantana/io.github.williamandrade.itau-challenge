package io.github.williamandradesantana.itau_challenge.infrastructure.persistence.transaction;

import io.github.williamandradesantana.itau_challenge.domain.transaction.Transaction;

public class TransactionMapper {

    public Transaction toDomain(TransactionJpaEntity entity) {
        return new Transaction(entity.getId(), entity.getAmount(), entity.getDateTime());
    }

    public TransactionJpaEntity toJpaEntity(Transaction transaction) {
        return new TransactionJpaEntity(transaction.getId(), transaction.getAmount(), transaction.getDateTime());
    }
}
