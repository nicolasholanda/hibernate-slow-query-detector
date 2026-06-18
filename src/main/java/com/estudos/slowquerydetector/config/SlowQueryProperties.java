package com.estudos.slowquerydetector.config;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

@Validated
@ConfigurationProperties(prefix = "slow-query")
public class SlowQueryProperties {

    private boolean enabled = true;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal warningThresholdMillis = new BigDecimal("500");

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal criticalThresholdMillis = new BigDecimal("2000");

    private boolean persistRecords = true;

    @Min(1)
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
