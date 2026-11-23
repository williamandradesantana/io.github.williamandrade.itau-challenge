package io.github.williamandradesantana.itau_challenge.application.transaction.dtos;

import java.math.BigDecimal;

public record CalculateTransactionStatisticsOutput(
        long count,
        BigDecimal sum,
        BigDecimal avg,
        BigDecimal min,
        BigDecimal max
) {
}
