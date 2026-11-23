package io.github.williamandradesantana.itau_challenge.domain.transaction;

import io.github.williamandradesantana.itau_challenge.domain.transaction.exceptions.InvalidTransactionAmountException;
import io.github.williamandradesantana.itau_challenge.domain.transaction.exceptions.InvalidTransactionDateTimeException;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public final class Transaction {

    private final UUID id;
    private BigDecimal amount;
    private OffsetDateTime dateTime;

    public Transaction(UUID id, BigDecimal amount, OffsetDateTime dateTime) {
        this.id = Objects.requireNonNull(id);
        setAmount(amount);
        setDateTime(dateTime);
    }

    public static Transaction create(BigDecimal amount, OffsetDateTime dateTime) {
        return new Transaction(UUID.randomUUID(), amount, dateTime);
    }

    public UUID getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) throw new InvalidTransactionAmountException();
        this.amount = amount;
    }

    public OffsetDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(OffsetDateTime dateTime) {
        if (dateTime == null) throw new InvalidTransactionDateTimeException("The datetime cannot be null!");

        OffsetDateTime now = OffsetDateTime.now(Clock.systemUTC());

        if (dateTime.isAfter(now))
            throw new InvalidTransactionDateTimeException("The datetime cannot be in the future");

        this.dateTime = dateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Transaction that = (Transaction) o;
        return id.equals(that.id) && Objects.equals(amount, that.amount) && Objects.equals(dateTime, that.dateTime);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + Objects.hashCode(amount);
        result = 31 * result + Objects.hashCode(dateTime);
        return result;
    }
}
