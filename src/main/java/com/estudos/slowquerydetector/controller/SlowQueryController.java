package com.estudos.slowquerydetector.controller;

import com.estudos.slowquerydetector.domain.QuerySeverity;
import com.estudos.slowquerydetector.domain.QueryType;
import com.estudos.slowquerydetector.domain.SlowQueryRecord;
import com.estudos.slowquerydetector.dto.RecordSlowQueryRequest;
import com.estudos.slowquerydetector.dto.SlowQueryDTO;
import com.estudos.slowquerydetector.exception.SlowQueryNotFoundException;
import com.estudos.slowquerydetector.service.SlowQueryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/slow-queries")
public class SlowQueryController {

    private final SlowQueryService service;

    public SlowQueryController(SlowQueryService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SlowQueryDTO> record(@Valid @RequestBody RecordSlowQueryRequest request) {
        Optional<SlowQueryRecord> persisted = service.record(
            request.statement(),
            request.executionTimeMillis(),
            request.threadName(),
            request.sourceClass()
        );
        return persisted
            .map(record -> ResponseEntity
                .created(URI.create("/api/slow-queries/" + record.getId()))
                .body(SlowQueryDTO.fromEntity(record)))
            .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping
    public Page<SlowQueryDTO> search(
        @RequestParam(required = false) QueryType queryType,
        @RequestParam(required = false) QuerySeverity severity,
        Pageable pageable
    ) {
        return service.search(queryType, severity, pageable).map(SlowQueryDTO::fromEntity);
    }

    @GetMapping("/{id}")
    public SlowQueryDTO findById(@PathVariable UUID id) {
        return service.findById(id)
            .map(SlowQueryDTO::fromEntity)
            .orElseThrow(() -> new SlowQueryNotFoundException(id));
    }

    @GetMapping("/by-hash/{hash}")
    public List<SlowQueryDTO> findByHash(@PathVariable String hash) {
        return service.findByStatementHash(hash).stream()
            .map(SlowQueryDTO::fromEntity)
            .toList();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable UUID id) {
        service.findById(id).orElseThrow(() -> new SlowQueryNotFoundException(id));
        service.deleteById(id);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAll() {
        service.deleteAll();
    }
}
