package com.koch.anomaly;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "KILN")
public class Kiln {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE", nullable = false)
    private KilnType type;

    @Column(name = "IS_ACTIVE", nullable = false)
    private Boolean isActive;

    @Column(name = "BASELINE_TEMP", nullable = false)
    private double baselineTemp;

    @Column(name = "WARNING_TEMP", nullable = false)
    private double warningTemp;

    @Column(name = "CRITICAL_TEMP", nullable = false)
    private double criticalTemp;

    @Column(name = "STATE_DURATION_SECONDS", nullable = false)
    private int stateDurationSeconds;

    @Column(name = "WARNING_PROBABILITY", nullable = false)
    private double warningProbability;

    @Column(name = "CRITICAL_PROBABILITY", nullable = false)
    private double criticalProbability;

    // Getters and Setters

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public KilnType getType() { return type; }
    public void setType(KilnType type) { this.type = type; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public double getBaselineTemp() { return baselineTemp; }
    public void setBaselineTemp(double baselineTemp) { this.baselineTemp = baselineTemp; }

    public double getWarningTemp() { return warningTemp; }
    public void setWarningTemp(double warningTemp) { this.warningTemp = warningTemp; }

    public double getCriticalTemp() { return criticalTemp; }
    public void setCriticalTemp(double criticalTemp) { this.criticalTemp = criticalTemp; }

    public int getStateDurationSeconds() { return stateDurationSeconds; }
    public void setStateDurationSeconds(int stateDurationSeconds) { this.stateDurationSeconds = stateDurationSeconds; }

    public double getWarningProbability() { return warningProbability; }
    public void setWarningProbability(double warningProbability) { this.warningProbability = warningProbability; }

    public double getCriticalProbability() { return criticalProbability; }
    public void setCriticalProbability(double criticalProbability) { this.criticalProbability = criticalProbability; }
}
