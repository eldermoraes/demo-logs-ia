package com.eldermoraes;

public record LogAnalysisResult(
        String severity,        // INFO, WARN, ERROR, CRITICAL
        String component,       // ex: PaymentService, AuthDb
        String errorType,       // ex: ConnectionTimeout, NullPointer
        String rootCauseSummary,// Explicação humana (max 10 palavras)
        String suggestedAction,  // ex: "Restart Pod", "Increase Pool", etc
        String timestamp        // ISO 8601 timestamp
) {}