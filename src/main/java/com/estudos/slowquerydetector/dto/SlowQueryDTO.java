package com.estudos.slowquerydetector.dto;

import com.estudos.slowquerydetector.domain.QuerySeverity;
import com.estudos.slowquerydetector.domain.QueryType;
import com.estudos.slowquerydetector.domain.SlowQueryRecord;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SlowQueryDTO(
    UUID id,
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

    public static SlowQueryDTO fromEntity(SlowQueryRecord record) {
        return new SlowQueryDTO(
            record.getId(),
            record.getStatement(),
            record.getStatementHash(),
            record.getQueryType(),
            record.getSeverity(),
            record.getExecutionTimeMillis(),
            record.getThresholdMillis(),
            record.getRecordedAt(),
            record.getThreadName(),
            record.getSourceClass()
        );
    }
}
