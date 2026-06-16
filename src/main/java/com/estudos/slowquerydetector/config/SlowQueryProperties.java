package com.estudos.slowquerydetector.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "slow-query")
public class SlowQueryProperties {

    private boolean enabled = true;
    private BigDecimal warningThresholdMillis = new BigDecimal("500");
    private BigDecimal criticalThresholdMillis = new BigDecimal("2000");
    private boolean persistRecords = true;
    private int maxStatementLength = 8000;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public BigDecimal getWarningThresholdMillis() {
        return warningThresholdMillis;
    }

    public void setWarningThresholdMillis(BigDecimal warningThresholdMillis) {
        this.warningThresholdMillis = warningThresholdMillis;
    }

    public BigDecimal getCriticalThresholdMillis() {
        return criticalThresholdMillis;
    }

    public void setCriticalThresholdMillis(BigDecimal criticalThresholdMillis) {
        this.criticalThresholdMillis = criticalThresholdMillis;
    }

    public boolean isPersistRecords() {
        return persistRecords;
    }

    public void setPersistRecords(boolean persistRecords) {
        this.persistRecords = persistRecords;
    }

    public int getMaxStatementLength() {
        return maxStatementLength;
    }

    public void setMaxStatementLength(int maxStatementLength) {
        this.maxStatementLength = maxStatementLength;
    }
}
