package com.eldermoraes;


public record LogAnalysisResult (
        Severity severity,       // INFO, WARN, ERROR, CRITICAL
        String component,        // ex: PaymentService, AuthDb
        String errorType,        // ex: ConnectionTimeout, NullPointer
        String rootCauseSummary, // Brief explanation (max 10 words)
        String suggestedAction,  // ex: "Restart Pod", "Increase Pool", etc
        String timestamp,
        String originalLog       // Original raw log entry
) {}