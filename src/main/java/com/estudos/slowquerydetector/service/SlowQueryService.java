package com.estudos.slowquerydetector.service;

import com.estudos.slowquerydetector.config.SlowQueryProperties;
import com.estudos.slowquerydetector.domain.QuerySeverity;
import com.estudos.slowquerydetector.domain.QueryType;
import com.estudos.slowquerydetector.domain.SlowQueryRecord;
import com.estudos.slowquerydetector.interceptor.QueryAnalyzer;
import com.estudos.slowquerydetector.repository.SlowQueryRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SlowQueryService {

    private static final Logger log = LoggerFactory.getLogger(SlowQueryService.class);

    private final SlowQueryRecordRepository repository;
    private final SlowQueryProperties properties;

    public SlowQueryService(SlowQueryRecordRepository repository, SlowQueryProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    @Transactional
    public Optional<SlowQueryRecord> record(
        String sql,
        BigDecimal elapsedMillis,
        String threadName,
        String sourceClass
    ) {
        if (!properties.isEnabled()) {
            return Optional.empty();
        }
        if (sql == null || sql.isBlank()) {
            return Optional.empty();
        }
        if (!QueryAnalyzer.exceedsThreshold(elapsedMillis, properties.getWarningThresholdMillis())) {
            return Optional.empty();
        }

        QueryType type = QueryAnalyzer.determineType(sql);
        QuerySeverity severity = QueryAnalyzer.severityFor(
            elapsedMillis,
            properties.getWarningThresholdMillis(),
            properties.getCriticalThresholdMillis()
        );
        String truncatedSql = QueryAnalyzer.truncate(sql, properties.getMaxStatementLength());
        String hash = QueryAnalyzer.computeHash(truncatedSql);

        log.warn(
            "Slow query detected [severity={}, type={}, elapsedMillis={}]: {}",
            severity, type, elapsedMillis, truncatedSql
        );

        if (!properties.isPersistRecords()) {
            return Optional.empty();
        }

        SlowQueryRecord record = new SlowQueryRecord(
            truncatedSql,
            hash,
            type,
            severity,
            elapsedMillis,
            properties.getWarningThresholdMillis(),
            Instant.now(),
            threadName,
            sourceClass
        );
        return Optional.of(repository.save(record));
    }

    @Transactional(readOnly = true)
    public Optional<SlowQueryRecord> findById(UUID id) {
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public Page<SlowQueryRecord> search(QueryType type, QuerySeverity severity, Pageable pageable) {
        if (type != null && severity != null) {
            return repository.findByQueryTypeAndSeverity(type, severity, pageable);
        }
        if (type != null) {
            return repository.findByQueryType(type, pageable);
        }
        if (severity != null) {
            return repository.findBySeverity(severity, pageable);
        }
        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<SlowQueryRecord> findRecordedBetween(Instant from, Instant to, Pageable pageable) {
        return repository.findByRecordedAtBetween(from, to, pageable);
    }

    @Transactional(readOnly = true)
    public List<SlowQueryRecord> findByStatementHash(String statementHash) {
        return repository.findByStatementHashOrderByRecordedAtDesc(statementHash);
    }

    @Transactional
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    @Transactional
    public void deleteAll() {
        repository.deleteAll();
    }
}
