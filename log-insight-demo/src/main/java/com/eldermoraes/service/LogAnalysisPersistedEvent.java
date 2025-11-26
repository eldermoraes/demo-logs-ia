package com.eldermoraes.service;

import com.eldermoraes.model.LogAnalysis;

/**
 * CDI event fired when a LogAnalysis entity is persisted
 */
public record LogAnalysisPersistedEvent(LogAnalysis analysis) {
}

// Made with Bob
