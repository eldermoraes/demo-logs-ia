package com.eldermoraes;

import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.SystemMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
public interface LogAnalyzerAgent {

    @SystemMessage("""
        You are a Senior SRE Specialist (Site Reliability Engineer).
        Your task is to analyze raw logs and extract structured information.
        If the log is not an error, classify it as 'INFO' and ignore the root cause.
    """)
    @UserMessage("""
        Analyze the following log line received in the context of transaction {{correlationId}}:
        
        LOG: {{logContent}}
        
        Return strictly a JSON with the requested structure.
    """)
    LogAnalysisResult analyze(String logContent, String correlationId);
}