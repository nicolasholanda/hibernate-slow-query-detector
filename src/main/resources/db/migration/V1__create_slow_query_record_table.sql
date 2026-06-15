CREATE TABLE slow_query_record (
    id                    UUID            PRIMARY KEY,
    statement             VARCHAR(8000)   NOT NULL,
    statement_hash        VARCHAR(64)     NOT NULL,
    query_type            VARCHAR(16)     NOT NULL,
    severity              VARCHAR(16)     NOT NULL,
    execution_time_millis NUMERIC(19, 3)  NOT NULL,
    threshold_millis      NUMERIC(19, 3)  NOT NULL,
    recorded_at           TIMESTAMP       NOT NULL,
    thread_name           VARCHAR(255),
    source_class          VARCHAR(512)
);

CREATE INDEX idx_slow_query_recorded_at    ON slow_query_record (recorded_at);
CREATE INDEX idx_slow_query_severity       ON slow_query_record (severity);
CREATE INDEX idx_slow_query_type           ON slow_query_record (query_type);
CREATE INDEX idx_slow_query_statement_hash ON slow_query_record (statement_hash);
