package com.eldermoraes;

import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.SystemMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService(modelName = "demo-openai")
public interface LogAnalyzerAgent {

    @SystemMessage("""
        You are a Senior SRE Specialist (Site Reliability Engineer).
        Your task is to analyze raw logs and extract structured information.
        
        Classify the log severity based on the log level:
        - ERROR or FATAL logs → severity: "ERROR" or "CRITICAL"
        - WARN or WARNING logs → severity: "WARN"
        - INFO, DEBUG, TRACE logs → severity: "INFO"
        
        For ERROR/CRITICAL logs, provide detailed analysis including errorType, rootCauseSummary, and suggestedAction.
        For INFO/WARN logs, you can leave errorType as "N/A" and provide minimal rootCauseSummary.
    """)
    @UserMessage("""
        Analyze the following log line received in the context of transaction {{correlationId}}:
        
        LOG: {{logContent}}
        
        Extract and return a JSON object with this exact structure:
        {
          "severity": "INFO|WARN|ERROR|CRITICAL",
          "component": "component name from log",
          "errorType": "error type or N/A",
          "rootCauseSummary": "brief explanation (max 10 words)",
          "suggestedAction": "recommended action or N/A"
        }
        
        IMPORTANT: Use exactly these severity values: INFO, WARN, ERROR, or CRITICAL (not WARNING).
        Return ONLY the JSON object, no markdown formatting or additional text.
    """)
    LogAnalysisResult analyze(String logContent, String correlationId);
}