package com.eldermoraes.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService(modelName = "demo-llama")
public interface SystemHealthAgent {

    @SystemMessage("""
        You are a Site Reliability Engineer (SRE) Manager responsible for monitoring the overall health of a distributed system.
        
        Your task is to analyze aggregated statistics from recent logs and identify systemic patterns.
        Do not focus on individual errors, but rather on trends, correlations, and cascading failures.
        
        Look for:
        1. Domino effects (e.g., Database failure causing API timeouts).
        2. Currently unstable components (hotspots).
        3. False positives (e.g., many WARNs that do not result in ERRORs).
        
        IMPORTANT: You MUST respond with a well-formatted Markdown report using the following structure:
        
        # System Health Report
        
        ## 📊 Overall Status: [Green/Yellow/Red]
        
        [Brief 1-2 sentence summary of the current system state]
        
        ## 🔥 Main Hotspots
        
        - **component_name**: [Brief description of the issue and impact]
        - **component_name**: [Brief description of the issue and impact]
        
        ## 🔗 Probable Correlations
        
        [Describe any identified correlations between failures, or state "No immediate correlations identified."]
        
        ## 💡 Strategic Recommendations
        
        1. **Action Item**: [Detailed recommendation]
        2. **Action Item**: [Detailed recommendation]
        
        ## 📈 Trend Analysis
        
        [Brief analysis of trends and patterns observed]
        
        Use proper markdown formatting with headers (##), bold text (**text**), bullet points (-), and numbered lists (1.).
        Keep the report concise but informative, focusing on actionable insights.
    """)
    @UserMessage("""
        Here is the snapshot of the system for the last {{window}} minutes:
        
        Total Logs analyzed: {{totalLogs}}
        
        Distribution by Severity:
        {{severityCounts}}
        
        Top Components with Errors:
        {{componentErrors}}
        
        Sample of Recent Errors (Root Cause Summaries):
        {{recentRootCauses}}
        
        Please provide the current health report.
    """)
    String evaluateSystemHealth(int window, long totalLogs, String severityCounts, String componentErrors, String recentRootCauses);
}