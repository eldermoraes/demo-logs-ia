package com.eldermoraes;

import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.OnClose;
import io.quarkus.websockets.next.WebSocket;
import io.quarkus.websockets.next.WebSocketConnection;
import org.jboss.logging.Logger;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket(path = "/logs/stream")
public class LogAnalysisWebSocket {

    private static final Logger LOG = Logger.getLogger(LogAnalysisWebSocket.class);
    
    // Store all active connections
    private static final Set<WebSocketConnection> connections = ConcurrentHashMap.newKeySet();

    @OnOpen
    public void onOpen(WebSocketConnection connection) {
        connections.add(connection);
        LOG.infof("WebSocket connection opened: %s. Total connections: %d",
                  connection.id(), connections.size());
    }

    @OnClose
    public void onClose(WebSocketConnection connection) {
        connections.remove(connection);
        LOG.infof("WebSocket connection closed: %s. Total connections: %d",
                  connection.id(), connections.size());
    }

    /**
     * Broadcast a log analysis result to all connected clients
     */
    public static void broadcast(LogAnalysisResult result) {
        String json = toJson(result);
        connections.forEach(connection -> {
            try {
                connection.sendTextAndAwait(json);
            } catch (Exception e) {
                LOG.errorf("Error sending message to connection %s: %s", connection.id(), e.getMessage());
            }
        });
    }

    /**
     * Simple JSON serialization for LogAnalysisResult
     */
    private static String toJson(LogAnalysisResult result) {
        return String.format(
            "{\"severity\":\"%s\",\"component\":\"%s\",\"errorType\":\"%s\",\"rootCauseSummary\":\"%s\",\"suggestedAction\":\"%s\",\"timestamp\":\"%s\",\"originalLog\":\"%s\"}",
            result.severity().name(),
            escapeJson(result.component()),
            escapeJson(result.errorType()),
            escapeJson(result.rootCauseSummary()),
            escapeJson(result.suggestedAction()),
            java.time.Instant.now().toString(),
            escapeJson(result.originalLog())
        );
    }

    private static String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }
}
