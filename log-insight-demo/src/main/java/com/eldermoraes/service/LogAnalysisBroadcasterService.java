package com.eldermoraes.service;

import com.eldermoraes.model.LogAnalysisResult;
import com.eldermoraes.model.LogAnalysis;
import com.eldermoraes.ui.LogAnalysisWebSocket;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import org.jboss.logging.Logger;

@ApplicationScoped
public class LogAnalysisBroadcasterService {
    
    private static final Logger LOG = Logger.getLogger(LogAnalysisBroadcasterService.class);
    
    /**
     * Observes LogAnalysis persistence events and broadcasts to WebSocket clients
     * Uses AFTER_SUCCESS to ensure the entity is committed before broadcasting
     */
    public void onLogAnalysisPersisted(@Observes(during = TransactionPhase.AFTER_SUCCESS) LogAnalysisPersistedEvent event) {
        try {
            LogAnalysis analysis = event.analysis();
            
            LogAnalysisResult result = new LogAnalysisResult(
                analysis.severity,
                analysis.component,
                analysis.errorType,
                analysis.rootCauseSummary,
                analysis.suggestedAction,
                analysis.timestamp,
                analysis.originalLog
            );
            
            LogAnalysisWebSocket.broadcast(result);
            LOG.debugf("Broadcasted log analysis for component: %s", analysis.component);
        } catch (Exception e) {
            LOG.errorf("Failed to broadcast log analysis: %s", e.getMessage());
        }
    }
}

// Made with Bob
