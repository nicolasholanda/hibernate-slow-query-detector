# hibernate-slow-query-detector

A Spring Boot service that intercepts SQL going through Hibernate, flags queries that take too long, and stores them so you can go look at what's actually hurting the database.

It plugs into Hibernate via a `StatementInspector`, so every statement Hibernate runs goes through it. When a query crosses a configurable threshold, it gets classified by type (`SELECT`, `INSERT`, `UPDATE`, `DELETE`, `OTHER`) and severity (`WARNING` or `CRITICAL`), then persisted with its execution time, a stable hash of the normalized SQL, and the thread + caller that triggered it. A small REST API on top lets you search, paginate, and pull aggregated stats — including the top offenders grouped by statement hash.

## Tech stack

- Java 21
- Spring Boot 3.4 (Web, Data JPA, Validation, Actuator)
- Hibernate 6 with a custom `StatementInspector`
- PostgreSQL at runtime, H2 in tests
- Flyway for schema migrations
- JUnit 5, Mockito, AssertJ

## Features

- Hibernate `StatementInspector` that captures every SQL with a per-thread context
- Configurable `warning` / `critical` thresholds and a kill-switch via `slow-query.enabled`
- Optional persistence (`slow-query.persist-records`) for when you just want logs
- SHA-256 hash of normalized SQL so the same query appearing many times groups cleanly
- REST endpoints to record, search, fetch, and delete slow queries
- Stats endpoint with severity/type breakdowns, max/avg execution time and top offenders for a time window
- Global exception handler with a consistent error response shape

## Configuration

`application.yml`:

```yaml
slow-query:
  enabled: true
  warning-threshold-millis: 500
  critical-threshold-millis: 2000
  persist-records: true
  max-statement-length: 8000
```

Database settings live under `spring.datasource` and `spring.flyway` — defaults assume a local Postgres at `localhost:5432/slow_query_detector`. Tests use the `test` profile and an in-memory H2.

## REST API

| Method | Path | Purpose |
|--------|------|---------|
| `POST` | `/api/slow-queries` | Record a slow query (returns 201 if persisted, 204 if below threshold) |
| `GET`  | `/api/slow-queries` | Paginated search, optional `queryType` and `severity` filters |
| `GET`  | `/api/slow-queries/{id}` | Fetch a single record |
| `GET`  | `/api/slow-queries/by-hash/{hash}` | All records for a given statement hash |
| `DELETE` | `/api/slow-queries/{id}` | Delete one |
| `DELETE` | `/api/slow-queries` | Wipe everything |
| `GET`  | `/api/slow-queries/stats` | Aggregated stats for a time range |
| `GET`  | `/api/slow-queries/stats/top-offenders` | Top N statement hashes by occurrences |

## Running it

You'll need Java 21, Maven, and a running Postgres (or just point it at H2).

```bash
mvn spring-boot:run
```

Tests:

```bash
mvn test
```

Build a jar:

```bash
mvn clean package
java -jar target/hibernate-slow-query-detector-0.0.1-SNAPSHOT.jar
```

Once it's up, `GET http://localhost:8080/actuator/health` to confirm, then start hitting the endpoints.
