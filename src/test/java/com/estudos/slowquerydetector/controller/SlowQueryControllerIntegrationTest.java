package com.estudos.slowquerydetector.controller;

import com.estudos.slowquerydetector.domain.QuerySeverity;
import com.estudos.slowquerydetector.domain.QueryType;
import com.estudos.slowquerydetector.domain.SlowQueryRecord;
import com.estudos.slowquerydetector.repository.SlowQueryRecordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.Instant;

import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SlowQueryControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private SlowQueryRecordRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        repository.deleteAll();
    }

    @Test
    void recordsSlowQueryAndReturnsCreated() throws Exception {
        String body = """
            {
              "statement": "SELECT * FROM users WHERE email = ?",
              "executionTimeMillis": 300.5,
              "threadName": "http-1",
              "sourceClass": "UserController"
            }
            """;

        mockMvc.perform(post("/api/slow-queries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.queryType").value("SELECT"))
            .andExpect(jsonPath("$.severity").value("CRITICAL"))
            .andExpect(jsonPath("$.executionTimeMillis").value(300.5));
    }

    @Test
    void returnsNoContentWhenBelowThreshold() throws Exception {
        String body = """
            {
              "statement": "SELECT 1",
              "executionTimeMillis": 5
            }
            """;

        mockMvc.perform(post("/api/slow-queries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isNoContent());
    }

    @Test
    void returnsBadRequestOnValidationFailure() throws Exception {
        String body = """
            {
              "statement": "",
              "executionTimeMillis": -1
            }
            """;

        mockMvc.perform(post("/api/slow-queries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.violations.length()").value(greaterThan(0)));
    }

    @Test
    void findByIdReturnsRecord() throws Exception {
        SlowQueryRecord saved = repository.save(sampleRecord("SELECT * FROM t", new BigDecimal("250"), QuerySeverity.CRITICAL));

        mockMvc.perform(get("/api/slow-queries/{id}", saved.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(saved.getId().toString()))
            .andExpect(jsonPath("$.queryType").value("SELECT"));
    }

    @Test
    void findByIdReturnsNotFoundForUnknownId() throws Exception {
        mockMvc.perform(get("/api/slow-queries/{id}", "00000000-0000-0000-0000-000000000000"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void searchFiltersByQueryTypeAndSeverity() throws Exception {
        repository.save(sampleRecord("SELECT * FROM t", new BigDecimal("250"), QuerySeverity.CRITICAL));
        repository.save(sampleRecord("UPDATE t SET a = 1", new BigDecimal("100"), QuerySeverity.WARNING));

        mockMvc.perform(get("/api/slow-queries")
                .param("queryType", "SELECT"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].queryType").value("SELECT"));
    }

    @Test
    void deleteByIdRemovesRecord() throws Exception {
        SlowQueryRecord saved = repository.save(sampleRecord("SELECT 1", new BigDecimal("250"), QuerySeverity.CRITICAL));

        mockMvc.perform(delete("/api/slow-queries/{id}", saved.getId()))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/slow-queries/{id}", saved.getId()))
            .andExpect(status().isNotFound());
    }

    private SlowQueryRecord sampleRecord(String sql, BigDecimal elapsed, QuerySeverity severity) {
        return new SlowQueryRecord(
            sql,
            "hash-" + sql.hashCode(),
            QueryType.valueOf(sql.stripLeading().split("\\s+")[0].toUpperCase()),
            severity,
            elapsed,
            new BigDecimal("50"),
            Instant.now(),
            "test-thread",
            "TestClass"
        );
    }
}
