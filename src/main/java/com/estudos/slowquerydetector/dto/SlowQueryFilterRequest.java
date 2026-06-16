package com.estudos.slowquerydetector.dto;

import com.estudos.slowquerydetector.domain.QuerySeverity;
import com.estudos.slowquerydetector.domain.QueryType;

import java.time.Instant;

public record SlowQueryFilterRequest(
    QueryType queryType,
    QuerySeverity severity,
    Instant from,
    Instant to
) {

    public boolean hasTimeRange() {
        return from != null && to != null;
    }
}
