package com.estudos.slowquerydetector.interceptor;

import com.estudos.slowquerydetector.domain.QuerySeverity;
import com.estudos.slowquerydetector.domain.QueryType;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class QueryAnalyzer {

    private QueryAnalyzer() {
    }

    public static QueryType determineType(String sql) {
        if (sql == null || sql.isBlank()) {
            return QueryType.OTHER;
        }
        String head = sql.stripLeading().toUpperCase();
        if (head.startsWith("SELECT")) return QueryType.SELECT;
        if (head.startsWith("INSERT")) return QueryType.INSERT;
        if (head.startsWith("UPDATE")) return QueryType.UPDATE;
        if (head.startsWith("DELETE")) return QueryType.DELETE;
        return QueryType.OTHER;
    }

    public static String computeHash(String sql) {
        if (sql == null) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(normalize(sql).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    public static String truncate(String sql, int max) {
        if (sql == null) {
            return null;
        }
        if (max <= 0 || sql.length() <= max) {
            return sql;
        }
        return sql.substring(0, max);
    }

    public static QuerySeverity severityFor(
        BigDecimal elapsedMillis,
        BigDecimal warningThreshold,
        BigDecimal criticalThreshold
    ) {
        if (elapsedMillis.compareTo(criticalThreshold) >= 0) {
            return QuerySeverity.CRITICAL;
        }
        if (elapsedMillis.compareTo(warningThreshold) >= 0) {
            return QuerySeverity.WARNING;
        }
        return QuerySeverity.NORMAL;
    }

    public static boolean exceedsThreshold(BigDecimal elapsedMillis, BigDecimal warningThreshold) {
        return elapsedMillis.compareTo(warningThreshold) >= 0;
    }

    private static String normalize(String sql) {
        return sql.replaceAll("\\s+", " ").trim();
    }
}
