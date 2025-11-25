package com.eldermoraes;

/**
 * CDI event fired when a LogAnalysis entity is persisted
 */
public record LogAnalysisPersistedEvent(LogAnalysis analysis) {
}

// Made with Bob
