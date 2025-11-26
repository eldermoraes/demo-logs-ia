package com.eldermoraes.resource;

import com.eldermoraes.ai.SystemHealthAgent;
import com.eldermoraes.model.LogAnalysis;
import com.eldermoraes.model.Severity;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/health-report")
@RunOnVirtualThread
public class SystemHealthResource {

    @Inject
    SystemHealthAgent healthAgent;

    @GET
    @Produces(MediaType.TEXT_PLAIN) // Returns pure Markdown for easy reading
    public String getCurrentHealthReport(@QueryParam("pageSize") @DefaultValue("50") int pageSize) {
        // 1. Define time window based on user-specified page size
        // Since timestamp is a String in the entity, we use the ID desc order to get the latest ones.

        // Validate page size (min 10, max 500)
        if (pageSize < 10) pageSize = 10;
        if (pageSize > 500) pageSize = 500;

        List<LogAnalysis> recentLogs = LogAnalysis.find("order by id desc").page(0, pageSize).list();

        if (recentLogs.isEmpty()) {
            return "Insufficient data for health analysis at the moment.";
        }

        // 2. Aggregate Data (Prepare context for AI)
        long totalLogs = recentLogs.size();

        // Count by Severity
        Map<Severity, Long> severityMap = recentLogs.stream()
                .collect(Collectors.groupingBy(log -> log.severity, Collectors.counting()));
        String severityCounts = severityMap.toString();

        // Top Components with issues (ERROR or CRITICAL)
        Map<String, Long> componentErrorMap = recentLogs.stream()
                .filter(log -> log.severity == Severity.ERROR || log.severity == Severity.CRITICAL)
                .collect(Collectors.groupingBy(log -> log.component, Collectors.counting()));

        String componentErrors = componentErrorMap.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())) // Descending order
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining(", "));

        if (componentErrors.isEmpty()) {
            componentErrors = "No components showing critical errors in the current window.";
        }

        // Sample of root causes (to provide context without exceeding token limits)
        String recentRootCauses = recentLogs.stream()
                .filter(log -> log.rootCauseSummary != null && !log.rootCauseSummary.isBlank())
                .map(log -> "[" + log.component + "] " + log.rootCauseSummary)
                .distinct()
                .limit(10)
                .collect(Collectors.joining("\n"));

        // 3. Call the Agent
        return healthAgent.evaluateSystemHealth(
                5, // Assuming an approximate time window based on log frequency
                totalLogs,
                severityCounts,
                componentErrors,
                recentRootCauses
        );
    }
}