package com.eldermoraes;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;

@RegisterAiService(modelName = "demo-llama")
@ApplicationScoped
public interface FailureGeneratorAgent {
    @SystemMessage("""
            You are a distributed systems log simulator.
            Generate ONE SINGLE log line in Prometheus metric format simulating a system event.
            
            Format structure: metric_name followed by labels in curly braces, then metric_value, then timestamp
            The labels should be enclosed in a single pair of curly braces with key="value" pairs separated by commas.
            
            RANDOMLY choose ONE severity level from: "CRITICAL", "ERROR", "WARNING", "INFO"
            
            Required labels: component, technology, severity, event_type
            - For severity="CRITICAL": use metric_name "system_critical_total", event_type examples: "service_crash", "data_corruption", "security_breach"
            - For severity="ERROR": use metric_name "system_error_total", event_type examples: "connection_timeout", "null_pointer", "authentication_failed"
            - For severity="WARNING": use metric_name "system_warning_total", event_type examples: "high_memory_usage", "slow_query", "deprecated_api_call"
            - For severity="INFO": use metric_name "system_info_total", event_type examples: "service_started", "configuration_loaded", "cache_cleared"
            
            The metric_value should be a realistic counter value based on severity:
            - CRITICAL: random value between 1-5 (rare but serious)
            - ERROR: random value between 5-50 (more common errors)
            - WARNING: random value between 20-200 (frequent warnings)
            - INFO: random value between 100-1000 (very frequent informational events)
            
            Include a Unix timestamp in milliseconds
            Do not include markdown or explanations. Only the raw Prometheus metric line.
            
            Example output format (use actual curly braces in your output):
            system_critical_total with labels component="database",technology="postgresql",severity="CRITICAL",event_type="data_corruption" value 3 timestamp 1732502299000
            system_error_total with labels component="api",technology="rest",severity="ERROR",event_type="connection_timeout" value 27 timestamp 1732502299000
            system_warning_total with labels component="cache",technology="redis",severity="WARNING",event_type="high_memory_usage" value 145 timestamp 1732502299000
            system_info_total with labels component="service",technology="kubernetes",severity="INFO",event_type="pod_started" value 523 timestamp 1732502299000
            
            IMPORTANT: In your actual output, replace "with labels" with opening curly brace, replace "value" with just the number, replace "timestamp" with just the number, and close the labels section with closing curly brace.
            IMPORTANT: Use uppercase severity values: CRITICAL, ERROR, WARNING, INFO (not lowercase).
            IMPORTANT: Make sure to always create an event with a different severity from the last one created.
        """)
    @UserMessage("Generate a realistic error for the {component} component using {tech} technology.")
    String generateErrorLog(@MemoryId Long id, String component, String tech);
}