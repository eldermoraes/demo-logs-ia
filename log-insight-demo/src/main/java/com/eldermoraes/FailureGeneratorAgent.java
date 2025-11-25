package com.eldermoraes;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService(modelName = "demo-llama")
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