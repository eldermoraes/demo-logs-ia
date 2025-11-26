package com.eldermoraes.resource;

import com.eldermoraes.model.LogAnalysis;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/api/logs")
public class LogAnalysisResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<LogAnalysis> getAllLogs() {
        // Return all logs ordered by ID descending (newest first)
        return LogAnalysis.find("order by id desc").list();
    }
}

// Made with Bob
