package com.estudos.slowquerydetector.dto;

import com.estudos.slowquerydetector.domain.QuerySeverity;
import com.estudos.slowquerydetector.domain.QueryType;
import com.estudos.slowquerydetector.service.SlowQueryStatisticsService.TopOffender;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public record SlowQueryStatsDTO(
    long totalRecords,
    Map<QuerySeverity, Long> countsBySeverity,
    Map<QueryType, Long> countsByQueryType,
    BigDecimal maxExecutionTimeMillis,
    BigDecimal averageExecutionTimeMillis,
    Instant from,
    Instant to,
    List<TopOffender> topOffenders
) {
}
