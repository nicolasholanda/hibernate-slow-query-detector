package com.estudos.slowquerydetector.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record RecordSlowQueryRequest(

    @NotBlank
    @Size(max = 8000)
    String statement,

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    BigDecimal executionTimeMillis,

    @Size(max = 255)
    String threadName,

    @Size(max = 512)
    String sourceClass
) {
}
