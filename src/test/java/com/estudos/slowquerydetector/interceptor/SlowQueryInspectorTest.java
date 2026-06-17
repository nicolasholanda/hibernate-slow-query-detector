package com.estudos.slowquerydetector.interceptor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SlowQueryInspectorTest {

    private final SlowQueryInspector inspector = new SlowQueryInspector();

    @AfterEach
    void cleanUp() {
        inspector.clear();
    }

    @Test
    void returnsSqlUnchangedOnInspect() {
        String sql = "SELECT 1";
        assertThat(inspector.inspect(sql)).isEqualTo(sql);
    }

    @Test
    void peekReturnsLastInspectedStatement() {
        inspector.inspect("SELECT 1");
        inspector.inspect("SELECT 2");
        assertThat(inspector.peek().sql()).isEqualTo("SELECT 2");
    }

    @Test
    void consumePopsInLifoOrder() {
        inspector.inspect("SELECT 1");
        inspector.inspect("SELECT 2");
        assertThat(inspector.consume().sql()).isEqualTo("SELECT 2");
        assertThat(inspector.consume().sql()).isEqualTo("SELECT 1");
        assertThat(inspector.consume()).isNull();
    }

    @Test
    void recordsStartNanos() {
        long before = System.nanoTime();
        inspector.inspect("SELECT 1");
        long after = System.nanoTime();

        long captured = inspector.peek().startNanos();
        assertThat(captured).isBetween(before, after);
    }

    @Test
    void contextIsIsolatedPerThread() throws Exception {
        inspector.inspect("FROM-MAIN");

        Thread other = new Thread(() -> {
            assertThat(inspector.peek()).isNull();
            inspector.inspect("FROM-OTHER");
            assertThat(inspector.peek().sql()).isEqualTo("FROM-OTHER");
            inspector.clear();
        });
        other.start();
        other.join();

        assertThat(inspector.peek().sql()).isEqualTo("FROM-MAIN");
    }

    @Test
    void clearEmptiesContext() {
        inspector.inspect("SELECT 1");
        inspector.clear();
        assertThat(inspector.peek()).isNull();
    }
}
