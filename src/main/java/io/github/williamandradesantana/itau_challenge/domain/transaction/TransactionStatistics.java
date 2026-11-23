package io.github.williamandradesantana.itau_challenge.domain.transaction;

import java.math.BigDecimal;

public record TransactionStatistics(
    long count,
    BigDecimal sum,
    BigDecimal avg,
    BigDecimal min,
    BigDecimal max
) {
}
