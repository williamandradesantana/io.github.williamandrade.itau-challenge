package io.github.williamandradesantana.itau_challenge.infrastructure.persistence.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface TransactionJpaRepository extends JpaRepository<TransactionJpaEntity, UUID> {

    @Query(value = """
        select
            count(*) as count,
            coalesce(sum(amount), 0) as sum,
            coalesce(avg(amount), 0) as avg,
            coalesce(min(amount), 0) as min,
            coalesce(max(amount), 0) as max
        from tb_transactions
        where datetime >= :startDateTime
    """, nativeQuery = true)
    TransactionStatisticsProjection calculateStatisticsSince(@Param("startDateTime") OffsetDateTime startDateTime);
}
