package com.eldermoraes;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

@Entity
public class LogAnalysis extends PanacheEntity {
    public Severity severity;       // INFO, WARN, ERROR, CRITICAL
    public String component;        // ex: PaymentService, AuthDb
    public String errorType;        // ex: ConnectionTimeout, NullPointer
    public String rootCauseSummary; // Brief explanation (max 10 words)
    public String suggestedAction;  // ex: "Restart Pod", "Increase Pool", etc
    public String timestamp;
    public String originalLog;      // Original raw log entry
}
