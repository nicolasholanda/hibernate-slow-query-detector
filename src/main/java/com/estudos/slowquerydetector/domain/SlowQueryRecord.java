package com.estudos.slowquerydetector.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
    name = "slow_query_record",
    indexes = {
        @Index(name = "idx_slow_query_recorded_at", columnList = "recorded_at"),
        @Index(name = "idx_slow_query_severity", columnList = "severity"),
        @Index(name = "idx_slow_query_type", columnList = "query_type"),
        @Index(name = "idx_slow_query_statement_hash", columnList = "statement_hash")
    }
)
public class SlowQueryRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "statement", nullable = false, length = 8000)
    private String statement;

    @Column(name = "statement_hash", nullable = false, length = 64)
    private String statementHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "query_type", nullable = false, length = 16)
    private QueryType queryType;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 16)
    private QuerySeverity severity;

    @Column(name = "execution_time_millis", nullable = false, precision = 19, scale = 3)
    private BigDecimal executionTimeMillis;

    @Column(name = "threshold_millis", nullable = false, precision = 19, scale = 3)
    private BigDecimal thresholdMillis;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @Column(name = "thread_name", length = 255)
    private String threadName;

    @Column(name = "source_class", length = 512)
    private String sourceClass;

    protected SlowQueryRecord() {
    }

    public SlowQueryRecord(
        String statement,
        String statementHash,
        QueryType queryType,
        QuerySeverity severity,
        BigDecimal executionTimeMillis,
        BigDecimal thresholdMillis,
        Instant recordedAt,
        String threadName,
        String sourceClass
    ) {
        this.statement = statement;
        this.statementHash = statementHash;
        this.queryType = queryType;
        this.severity = severity;
        this.executionTimeMillis = executionTimeMillis;
        this.thresholdMillis = thresholdMillis;
        this.recordedAt = recordedAt;
        this.threadName = threadName;
        this.sourceClass = sourceClass;
    }

    public UUID getId() {
        return id;
    }

    public String getStatement() {
        return statement;
    }

    public String getStatementHash() {
        return statementHash;
    }

    public QueryType getQueryType() {
        return queryType;
    }

    public QuerySeverity getSeverity() {
        return severity;
    }

    public BigDecimal getExecutionTimeMillis() {
        return executionTimeMillis;
    }

    public BigDecimal getThresholdMillis() {
        return thresholdMillis;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }

    public String getThreadName() {
        return threadName;
    }

    public String getSourceClass() {
        return sourceClass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SlowQueryRecord that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
