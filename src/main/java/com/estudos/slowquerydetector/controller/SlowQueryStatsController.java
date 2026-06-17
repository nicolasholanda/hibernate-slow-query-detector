package com.estudos.slowquerydetector.controller;

import com.estudos.slowquerydetector.dto.SlowQueryStatsDTO;
import com.estudos.slowquerydetector.exception.InvalidQueryFilterException;
import com.estudos.slowquerydetector.service.SlowQueryStatisticsService;
import com.estudos.slowquerydetector.service.SlowQueryStatisticsService.TopOffender;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/slow-queries/stats")
public class SlowQueryStatsController {

    private final SlowQueryStatisticsService statisticsService;

    public SlowQueryStatsController(SlowQueryStatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping
    public SlowQueryStatsDTO stats(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
        @RequestParam(defaultValue = "10") int topLimit
    ) {
        Instant resolvedTo = to != null ? to : Instant.now();
        Instant resolvedFrom = from != null ? from : resolvedTo.minus(24, ChronoUnit.HOURS);

        if (resolvedFrom.isAfter(resolvedTo)) {
            throw new InvalidQueryFilterException("'from' must be before or equal to 'to'");
        }
        if (topLimit <= 0 || topLimit > 100) {
            throw new InvalidQueryFilterException("'topLimit' must be between 1 and 100");
        }

        return new SlowQueryStatsDTO(
            statisticsService.totalRecords(),
            statisticsService.countsBySeverity(),
            statisticsService.countsByQueryType(),
            statisticsService.maxExecutionTimeBetween(resolvedFrom, resolvedTo),
            statisticsService.averageExecutionTimeBetween(resolvedFrom, resolvedTo),
            resolvedFrom,
            resolvedTo,
            statisticsService.topOffendersBetween(resolvedFrom, resolvedTo, topLimit)
        );
    }

    @GetMapping("/top-offenders")
    public List<TopOffender> topOffenders(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
        @RequestParam(defaultValue = "10") int limit
    ) {
        Instant resolvedTo = to != null ? to : Instant.now();
        Instant resolvedFrom = from != null ? from : resolvedTo.minus(24, ChronoUnit.HOURS);
        if (limit <= 0 || limit > 100) {
            throw new InvalidQueryFilterException("'limit' must be between 1 and 100");
        }
        return statisticsService.topOffendersBetween(resolvedFrom, resolvedTo, limit);
    }
}
