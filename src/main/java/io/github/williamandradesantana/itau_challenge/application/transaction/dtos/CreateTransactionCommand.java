package io.github.williamandradesantana.itau_challenge.application.transaction.dtos;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CreateTransactionCommand(BigDecimal amount, OffsetDateTime datetime) {
}
