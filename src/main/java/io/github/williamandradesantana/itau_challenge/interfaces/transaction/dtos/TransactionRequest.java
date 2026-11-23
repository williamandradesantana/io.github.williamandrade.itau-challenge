package io.github.williamandradesantana.itau_challenge.interfaces.transaction.dtos;

import io.github.williamandradesantana.itau_challenge.application.transaction.dtos.CreateTransactionCommand;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TransactionRequest(@NotNull BigDecimal amount, @NotNull OffsetDateTime dateTime) {
    public static TransactionRequest from(CreateTransactionCommand command) {
        return new TransactionRequest(command.amount(), command.datetime());
    }
}
