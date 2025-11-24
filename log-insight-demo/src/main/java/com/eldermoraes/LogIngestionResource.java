package com.eldermoraes;

import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.MediaType;
import java.lang.ScopedValue;
import java.util.UUID;
import org.jboss.logging.Logger;

@Path("/ingest")
public class LogIngestionResource {

    private static final Logger LOG = Logger.getLogger(LogIngestionResource.class);

    @Inject
    LogAnalyzerAgent analyzer;

    // JEP 444: Executes each request in a new lightweight Virtual Thread.
    // Allows ingesting thousands of simultaneous logs without blocking OS threads.
    @POST
    @RunOnVirtualThread
    @Consumes(MediaType.TEXT_PLAIN)
    public LogAnalysisResult ingestLog(String rawLog) {

        String batchId = UUID.randomUUID().toString();

        // JEP 506: Scoped Value binding.
        // The 'batchId' value is available only within the scope of the runnable below
        // and its child threads (Structured Concurrency), with no copying cost.
        return ScopedValue.where(ContextConfig.CORRELATION_ID, batchId).call(() -> {

            // Retrieves the ID from the ScopedValue inside the business method
            String currentId = ContextConfig.CORRELATION_ID.get();
            LOG.infof("Processing log on Virtual Thread: %s | ID: %s",
                    Thread.currentThread(), currentId);

            // Calls the AI to structure the data
            // In production, we would check a vector store (e.g., PgVector) before calling the LLM
            return analyzer.analyze(rawLog, currentId);
        });
    }
}