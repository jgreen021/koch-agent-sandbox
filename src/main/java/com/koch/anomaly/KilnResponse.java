package com.koch.anomaly;

import java.util.UUID;

/**
 * Immutable DTO for mapping Kiln payload responses to the UI client.
 */
public record KilnResponse(
    UUID id,
    String name,
    KilnType type,
    Boolean isActive,
    Double baselineTemp,
    Double warningTemp,
    Double criticalTemp,
    Integer stateDurationSeconds,
    Double warningProbability,
    Double criticalProbability,
    Double normalProbability
) {
    public static KilnResponse fromRequest(UUID id, KilnRequest req) {
        return new KilnResponse(
            id,
            req.name(),
            req.type(),
            req.isActive(),
            req.baselineTemp(),
            req.warningTemp(),
            req.criticalTemp(),
            req.stateDurationSeconds(),
            req.warningProbability(),
            req.criticalProbability(),
            // Implicit calculation explicitly returned for the UI
            1.0 - (req.warningProbability() + req.criticalProbability())
        );
    }
}
