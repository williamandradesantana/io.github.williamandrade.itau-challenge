package io.github.williamandradesantana.itau_challenge.infrastructure.persistence.transaction;

import java.math.BigDecimal;

public interface TransactionStatisticsProjection {
    Long getCount();
    BigDecimal getSum();
    BigDecimal getAvg();
    BigDecimal getMin();
    BigDecimal getMax();
}
