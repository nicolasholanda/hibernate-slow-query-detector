package com.estudos.slowquerydetector.repository;

import com.estudos.slowquerydetector.domain.QuerySeverity;
import com.estudos.slowquerydetector.domain.QueryType;
import com.estudos.slowquerydetector.domain.SlowQueryRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface SlowQueryRecordRepository extends JpaRepository<SlowQueryRecord, UUID> {

    Page<SlowQueryRecord> findByQueryType(QueryType queryType, Pageable pageable);

    Page<SlowQueryRecord> findBySeverity(QuerySeverity severity, Pageable pageable);

    Page<SlowQueryRecord> findByQueryTypeAndSeverity(QueryType queryType, QuerySeverity severity, Pageable pageable);

    Page<SlowQueryRecord> findByRecordedAtBetween(Instant from, Instant to, Pageable pageable);

    List<SlowQueryRecord> findByStatementHashOrderByRecordedAtDesc(String statementHash);

    long countBySeverity(QuerySeverity severity);

    long countByQueryType(QueryType queryType);

    @Query("""
        SELECT MAX(s.executionTimeMillis)
        FROM SlowQueryRecord s
        WHERE s.recordedAt BETWEEN :from AND :to
    """)
    BigDecimal findMaxExecutionTimeBetween(@Param("from") Instant from, @Param("to") Instant to);

    @Query("""
        SELECT AVG(s.executionTimeMillis)
        FROM SlowQueryRecord s
        WHERE s.recordedAt BETWEEN :from AND :to
    """)
    BigDecimal findAverageExecutionTimeBetween(@Param("from") Instant from, @Param("to") Instant to);

    @Query("""
        SELECT s.statementHash, COUNT(s)
        FROM SlowQueryRecord s
        WHERE s.recordedAt BETWEEN :from AND :to
        GROUP BY s.statementHash
        ORDER BY COUNT(s) DESC
    """)
    List<Object[]> findTopOffendersBetween(@Param("from") Instant from, @Param("to") Instant to, Pageable pageable);
}
