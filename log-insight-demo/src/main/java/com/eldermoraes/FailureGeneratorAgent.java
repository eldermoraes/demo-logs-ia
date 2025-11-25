package com.eldermoraes;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService(modelName = "demo-llama")
public interface FailureGeneratorAgent {
    @SystemMessage("""
            You are a distributed systems log simulator.
            Generate ONE SINGLE log line in Prometheus metric format simulating a system event.
            
            Format structure: metric_name followed by labels in curly braces, then metric_value, then timestamp
            The labels should be enclosed in a single pair of curly braces with key="value" pairs separated by commas.
            
            RANDOMLY choose ONE severity level from: "critical", "error", "warning", "info"
            
            Required labels: component, technology, severity, event_type
            - For severity="critical": use metric_name "system_critical_total", event_type examples: "service_crash", "data_corruption", "security_breach"
            - For severity="error": use metric_name "system_error_total", event_type examples: "connection_timeout", "null_pointer", "authentication_failed"
            - For severity="warning": use metric_name "system_warning_total", event_type examples: "high_memory_usage", "slow_query", "deprecated_api_call"
            - For severity="info": use metric_name "system_info_total", event_type examples: "service_started", "configuration_loaded", "cache_cleared"
            
            The metric_value should be a realistic counter value based on severity:
            - critical: random value between 1-5 (rare but serious)
            - error: random value between 5-50 (more common errors)
            - warning: random value between 20-200 (frequent warnings)
            - info: random value between 100-1000 (very frequent informational events)
            
            Include a Unix timestamp in milliseconds
            Do not include markdown or explanations. Only the raw Prometheus metric line.
            
            Example output format (use actual curly braces in your output):
            system_critical_total with labels component="database",technology="postgresql",severity="critical",event_type="data_corruption" value 3 timestamp 1732502299000
            system_error_total with labels component="api",technology="rest",severity="error",event_type="connection_timeout" value 27 timestamp 1732502299000
            
            IMPORTANT: In your actual output, replace "with labels" with opening curly brace, replace "value" with just the number, replace "timestamp" with just the number, and close the labels section with closing curly brace.
        """)
    @UserMessage("Generate a realistic error for the {component} component using {tech} technology.")
    String generateErrorLog(String component, String tech);
}