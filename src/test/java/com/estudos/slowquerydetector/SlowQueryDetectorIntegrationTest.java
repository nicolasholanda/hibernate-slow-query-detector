package com.estudos.slowquerydetector;

import com.estudos.slowquerydetector.config.SlowQueryProperties;
import com.estudos.slowquerydetector.domain.QuerySeverity;
import com.estudos.slowquerydetector.domain.QueryType;
import com.estudos.slowquerydetector.domain.SlowQueryRecord;
import com.estudos.slowquerydetector.interceptor.SlowQueryInspector;
import com.estudos.slowquerydetector.repository.SlowQueryRecordRepository;
import com.estudos.slowquerydetector.service.SlowQueryService;
import com.estudos.slowquerydetector.service.SlowQueryStatisticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class SlowQueryDetectorIntegrationTest {

    @Autowired
    private SlowQueryService service;

    @Autowired
    private SlowQueryStatisticsService statisticsService;

    @Autowired
    private SlowQueryRecordRepository repository;

    @Autowired
    private SlowQueryInspector inspector;

    @Autowired
    private SlowQueryProperties properties;

    @BeforeEach
    void cleanState() {
        repository.deleteAll();
        inspector.clear();
    }

    @Test
    void inspectorIsWiredIntoHibernate() {
        repository.count();
        SlowQueryInspector.InspectedStatement statement = inspector.peek();
        assertThat(statement).isNotNull();
        assertThat(statement.sql()).containsIgnoringCase("slow_query_record");
    }

    @Test
    void recordsSlowQueryEndToEndAndPersistsInDatabase() {
        Optional<SlowQueryRecord> persisted = service.record(
            "SELECT * FROM accounts WHERE id = 1",
            new BigDecimal("750"),
            "http-1",
            "AccountService"
        );

        assertThat(persisted).isPresent();
        SlowQueryRecord saved = persisted.get();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getQueryType()).isEqualTo(QueryType.SELECT);
        assertThat(saved.getSeverity()).isEqualTo(QuerySeverity.CRITICAL);

        SlowQueryRecord reloaded = repository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getStatement()).isEqualTo("SELECT * FROM accounts WHERE id = 1");
        assertThat(reloaded.getExecutionTimeMillis()).isEqualByComparingTo("750");
    }

    @Test
    void statisticsAggregateAcrossRecords() {
        service.record("SELECT 1", new BigDecimal("100"), "t", "C");
        service.record("UPDATE t SET a = 1", new BigDecimal("300"), "t", "C");
        service.record("SELECT 2", new BigDecimal("700"), "t", "C");

        long total = statisticsService.totalRecords();
        assertThat(total).isEqualTo(3);

        var bySeverity = statisticsService.countsBySeverity();
        assertThat(bySeverity.get(QuerySeverity.CRITICAL)).isEqualTo(2);
        assertThat(bySeverity.get(QuerySeverity.WARNING)).isEqualTo(1);

        var byType = statisticsService.countsByQueryType();
        assertThat(byType.get(QueryType.SELECT)).isEqualTo(2);
        assertThat(byType.get(QueryType.UPDATE)).isEqualTo(1);

        Instant to = Instant.now().plus(1, ChronoUnit.MINUTES);
        Instant from = to.minus(1, ChronoUnit.HOURS);
        assertThat(statisticsService.maxExecutionTimeBetween(from, to))
            .isEqualByComparingTo("700");
        assertThat(statisticsService.averageExecutionTimeBetween(from, to))
            .isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    void topOffendersGroupBySameStatementHash() {
        service.record("SELECT * FROM users WHERE id = 1", new BigDecimal("600"), "t", "C");
        service.record("SELECT   *   FROM users WHERE id = 1", new BigDecimal("700"), "t", "C");
        service.record("UPDATE users SET name = 'x'", new BigDecimal("800"), "t", "C");

        Instant to = Instant.now().plus(1, ChronoUnit.MINUTES);
        Instant from = to.minus(1, ChronoUnit.HOURS);

        var offenders = statisticsService.topOffendersBetween(from, to, 10);
        assertThat(offenders).hasSize(2);
        assertThat(offenders.get(0).occurrences()).isEqualTo(2);
    }

    @Test
    void propertiesAreLoadedFromTestProfile() {
        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.getWarningThresholdMillis()).isEqualByComparingTo("50");
        assertThat(properties.getCriticalThresholdMillis()).isEqualByComparingTo("200");
    }
}
