package com.estudos.slowquerydetector.service;

import com.estudos.slowquerydetector.domain.QuerySeverity;
import com.estudos.slowquerydetector.domain.QueryType;
import com.estudos.slowquerydetector.repository.SlowQueryRecordRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class SlowQueryStatisticsService {

    private final SlowQueryRecordRepository repository;

    public SlowQueryStatisticsService(SlowQueryRecordRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public long totalRecords() {
        return repository.count();
    }

    @Transactional(readOnly = true)
    public Map<QuerySeverity, Long> countsBySeverity() {
        Map<QuerySeverity, Long> result = new EnumMap<>(QuerySeverity.class);
        for (QuerySeverity severity : QuerySeverity.values()) {
            result.put(severity, repository.countBySeverity(severity));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public Map<QueryType, Long> countsByQueryType() {
        Map<QueryType, Long> result = new EnumMap<>(QueryType.class);
        for (QueryType type : QueryType.values()) {
            result.put(type, repository.countByQueryType(type));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public BigDecimal maxExecutionTimeBetween(Instant from, Instant to) {
        BigDecimal max = repository.findMaxExecutionTimeBetween(from, to);
        return max == null ? BigDecimal.ZERO : max;
    }

    @Transactional(readOnly = true)
    public BigDecimal averageExecutionTimeBetween(Instant from, Instant to) {
        BigDecimal avg = repository.findAverageExecutionTimeBetween(from, to);
        return avg == null ? BigDecimal.ZERO : avg;
    }

    @Transactional(readOnly = true)
    public List<TopOffender> topOffendersBetween(Instant from, Instant to, int limit) {
        List<Object[]> rows = repository.findTopOffendersBetween(from, to, PageRequest.of(0, limit));
        return rows.stream()
            .map(row -> new TopOffender((String) row[0], ((Number) row[1]).longValue()))
            .toList();
    }

    public record TopOffender(String statementHash, long occurrences) {
    }
}
