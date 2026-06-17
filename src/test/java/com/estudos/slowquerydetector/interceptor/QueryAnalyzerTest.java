package com.estudos.slowquerydetector.interceptor;

import com.estudos.slowquerydetector.domain.QuerySeverity;
import com.estudos.slowquerydetector.domain.QueryType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class QueryAnalyzerTest {

    private static final BigDecimal WARN = new BigDecimal("500");
    private static final BigDecimal CRITICAL = new BigDecimal("2000");

    @Test
    void determinesSelect() {
        assertThat(QueryAnalyzer.determineType("SELECT * FROM users")).isEqualTo(QueryType.SELECT);
        assertThat(QueryAnalyzer.determineType("  select id from t")).isEqualTo(QueryType.SELECT);
    }

    @Test
    void determinesInsertUpdateDelete() {
        assertThat(QueryAnalyzer.determineType("INSERT INTO t VALUES (1)")).isEqualTo(QueryType.INSERT);
        assertThat(QueryAnalyzer.determineType("update t set a=1")).isEqualTo(QueryType.UPDATE);
        assertThat(QueryAnalyzer.determineType("DELETE FROM t")).isEqualTo(QueryType.DELETE);
    }

    @Test
    void determinesOtherForUnknownAndBlank() {
        assertThat(QueryAnalyzer.determineType("CREATE TABLE t (id INT)")).isEqualTo(QueryType.OTHER);
        assertThat(QueryAnalyzer.determineType("")).isEqualTo(QueryType.OTHER);
        assertThat(QueryAnalyzer.determineType(null)).isEqualTo(QueryType.OTHER);
    }

    @Test
    void computesStableHashIgnoringWhitespace() {
        String a = QueryAnalyzer.computeHash("SELECT * FROM users WHERE id = 1");
        String b = QueryAnalyzer.computeHash("SELECT   *   FROM   users   WHERE   id = 1");
        assertThat(a).isEqualTo(b).hasSize(64);
    }

    @Test
    void hashOfNullIsEmpty() {
        assertThat(QueryAnalyzer.computeHash(null)).isEmpty();
    }

    @Test
    void truncatesWhenLargerThanMax() {
        assertThat(QueryAnalyzer.truncate("abcdef", 4)).isEqualTo("abcd");
        assertThat(QueryAnalyzer.truncate("abc", 10)).isEqualTo("abc");
        assertThat(QueryAnalyzer.truncate(null, 10)).isNull();
    }

    @Test
    void severityIsNormalBelowWarning() {
        assertThat(QueryAnalyzer.severityFor(new BigDecimal("100"), WARN, CRITICAL))
            .isEqualTo(QuerySeverity.NORMAL);
    }

    @Test
    void severityIsWarningAtAndAboveWarn() {
        assertThat(QueryAnalyzer.severityFor(new BigDecimal("500"), WARN, CRITICAL))
            .isEqualTo(QuerySeverity.WARNING);
        assertThat(QueryAnalyzer.severityFor(new BigDecimal("1500"), WARN, CRITICAL))
            .isEqualTo(QuerySeverity.WARNING);
    }

    @Test
    void severityIsCriticalAtAndAboveCritical() {
        assertThat(QueryAnalyzer.severityFor(new BigDecimal("2000"), WARN, CRITICAL))
            .isEqualTo(QuerySeverity.CRITICAL);
        assertThat(QueryAnalyzer.severityFor(new BigDecimal("9999"), WARN, CRITICAL))
            .isEqualTo(QuerySeverity.CRITICAL);
    }

    @Test
    void exceedsThresholdRespectsBoundary() {
        assertThat(QueryAnalyzer.exceedsThreshold(new BigDecimal("499"), WARN)).isFalse();
        assertThat(QueryAnalyzer.exceedsThreshold(new BigDecimal("500"), WARN)).isTrue();
        assertThat(QueryAnalyzer.exceedsThreshold(new BigDecimal("501"), WARN)).isTrue();
    }
}
