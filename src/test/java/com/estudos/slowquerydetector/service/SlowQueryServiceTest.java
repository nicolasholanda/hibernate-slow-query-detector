package com.estudos.slowquerydetector.service;

import com.estudos.slowquerydetector.config.SlowQueryProperties;
import com.estudos.slowquerydetector.domain.QuerySeverity;
import com.estudos.slowquerydetector.domain.QueryType;
import com.estudos.slowquerydetector.domain.SlowQueryRecord;
import com.estudos.slowquerydetector.repository.SlowQueryRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SlowQueryServiceTest {

    @Mock
    private SlowQueryRecordRepository repository;

    private SlowQueryProperties properties;

    private SlowQueryService service;

    @BeforeEach
    void setUp() {
        properties = new SlowQueryProperties();
        properties.setEnabled(true);
        properties.setPersistRecords(true);
        properties.setWarningThresholdMillis(new BigDecimal("500"));
        properties.setCriticalThresholdMillis(new BigDecimal("2000"));
        properties.setMaxStatementLength(8000);
        service = new SlowQueryService(repository, properties);
    }

    @Test
    void ignoresWhenDisabled() {
        properties.setEnabled(false);

        Optional<SlowQueryRecord> result = service.record("SELECT 1", new BigDecimal("999"), "main", "Caller");

        assertThat(result).isEmpty();
        verify(repository, never()).save(any());
    }

    @Test
    void ignoresWhenSqlIsBlank() {
        Optional<SlowQueryRecord> result = service.record("   ", new BigDecimal("999"), "main", "Caller");
        assertThat(result).isEmpty();
        verify(repository, never()).save(any());
    }

    @Test
    void ignoresWhenBelowWarningThreshold() {
        Optional<SlowQueryRecord> result = service.record("SELECT 1", new BigDecimal("100"), "main", "Caller");
        assertThat(result).isEmpty();
        verify(repository, never()).save(any());
    }

    @Test
    void persistsAsWarningWhenBetweenThresholds() {
        when(repository.save(any(SlowQueryRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<SlowQueryRecord> result = service.record(
            "SELECT * FROM users WHERE email = ?", new BigDecimal("750"), "http-1", "UserController"
        );

        assertThat(result).isPresent();
        ArgumentCaptor<SlowQueryRecord> captor = ArgumentCaptor.forClass(SlowQueryRecord.class);
        verify(repository).save(captor.capture());

        SlowQueryRecord saved = captor.getValue();
        assertThat(saved.getSeverity()).isEqualTo(QuerySeverity.WARNING);
        assertThat(saved.getQueryType()).isEqualTo(QueryType.SELECT);
        assertThat(saved.getExecutionTimeMillis()).isEqualByComparingTo("750");
        assertThat(saved.getThreadName()).isEqualTo("http-1");
        assertThat(saved.getSourceClass()).isEqualTo("UserController");
        assertThat(saved.getStatementHash()).hasSize(64);
        assertThat(saved.getRecordedAt()).isNotNull();
    }

    @Test
    void persistsAsCriticalAboveCriticalThreshold() {
        when(repository.save(any(SlowQueryRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<SlowQueryRecord> result = service.record(
            "UPDATE orders SET status = 'X'", new BigDecimal("5000"), "http-2", "OrderService"
        );

        assertThat(result).isPresent();
        assertThat(result.get().getSeverity()).isEqualTo(QuerySeverity.CRITICAL);
        assertThat(result.get().getQueryType()).isEqualTo(QueryType.UPDATE);
    }

    @Test
    void doesNotPersistWhenPersistRecordsDisabled() {
        properties.setPersistRecords(false);

        Optional<SlowQueryRecord> result = service.record(
            "SELECT 1", new BigDecimal("999"), "main", "Caller"
        );

        assertThat(result).isEmpty();
        verify(repository, never()).save(any());
    }

    @Test
    void truncatesStatementToConfiguredLimit() {
        properties.setMaxStatementLength(20);
        when(repository.save(any(SlowQueryRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        String longSql = "SELECT * FROM very_long_table_name WHERE column_a = 1 AND column_b = 2";
        Optional<SlowQueryRecord> result = service.record(longSql, new BigDecimal("800"), "t", "C");

        assertThat(result).isPresent();
        assertThat(result.get().getStatement()).hasSize(20);
    }
}
