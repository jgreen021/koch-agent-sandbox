package com.koch.anomaly;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Immutable DTO for mapping Kiln payload requests (Create/Update).
 */
public record KilnRequest(
    @NotBlank(message = "Kiln name is required")
    String name,
    
    @NotNull(message = "Kiln type is required")
    KilnType type,
    
    @NotNull(message = "Active status must be specified")
    Boolean isActive,
    
    @NotNull(message = "Baseline temperature is required")
    @Positive(message = "Baseline temperature must be positive")
    Double baselineTemp,
    
    @NotNull(message = "Warning temperature is required")
    @Positive(message = "Warning temperature must be positive")
    Double warningTemp,
    
    @NotNull(message = "Critical temperature is required")
    @Positive(message = "Critical temperature must be positive")
    Double criticalTemp,
    
    @NotNull(message = "State duration must be specified")
    @Positive(message = "State duration must be greater than 0")
    Integer stateDurationSeconds,
    
    @NotNull(message = "Warning probability is required")
    @DecimalMin(value = "0.0", message = "Warning probability must be at least 0.0")
    @DecimalMax(value = "1.0", message = "Warning probability cannot exceed 1.0")
    Double warningProbability,
    
    @NotNull(message = "Critical probability is required")
    @DecimalMin(value = "0.0", message = "Critical probability must be at least 0.0")
    @DecimalMax(value = "1.0", message = "Critical probability cannot exceed 1.0")
    Double criticalProbability
) {
    public KilnRequest {
        if (warningProbability + criticalProbability > 1.0) {
            throw new IllegalArgumentException("Combined anomaly probabilities cannot exceed 100% (1.0)");
        }
        if (warningTemp >= criticalTemp) {
            throw new IllegalArgumentException("Warning temperature must be lower than critical temperature");
        }
        if (baselineTemp >= warningTemp) {
            throw new IllegalArgumentException("Baseline temperature must be lower than warning temperature");
        }
    }
}
