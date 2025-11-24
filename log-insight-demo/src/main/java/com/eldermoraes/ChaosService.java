package com.eldermoraes;

import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.SystemMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.quarkus.scheduler.Scheduled;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ChaosService {

    private static final Logger LOG = Logger.getLogger(ChaosService.class);

    @Inject
    FailureGeneratorAgent agent;

    // Emulates a log flow on each 1s
    @Scheduled(every = "1s")
    void generateChaos() {
        // Asks AI to create a realistic error log
        String logEntry = agent.generateErrorLog("database_connection_pool", "postgres");

        // IRL, this could be sent to stdout or even Kafka
        LOG.error(logEntry);
    }

    @RegisterAiService
    public interface FailureGeneratorAgent {
        @SystemMessage("""
            You are a distributed systems failure simulator.
            Generate ONE SINGLE Java log line (Log4j format) simulating a critical error.
            Include timestamp, thread name, level (ERROR), and a short but realistic stack trace.
            Do not include markdown or explanations. Only the raw log.
        """)
        @UserMessage("Generate a realistic error for the {component} component using {tech} technology.")
        String generateErrorLog(String component, String tech);
    }
}