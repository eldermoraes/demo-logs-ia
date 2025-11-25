package com.eldermoraes;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.quarkus.scheduler.Scheduled;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Map;
import java.util.Random;

@ApplicationScoped
public class ChaosService {

    private static final Logger LOG = Logger.getLogger(ChaosService.class);
    private final Random random = new Random();

    @Inject
    LogAnalyzerAgent analyzer;

    // Paired component-tech mapping for realistic error scenarios
    private static final Map<String, List<String>> COMPONENT_TECH_MAP = Map.ofEntries(
        Map.entry("database_connection_pool", List.of("postgres", "mysql", "mongodb", "oracle", "mariadb")),
        Map.entry("cache_manager", List.of("redis", "memcached", "hazelcast", "ehcache")),
        Map.entry("message_queue_consumer", List.of("kafka", "rabbitmq", "activemq", "aws-sqs", "azure-servicebus")),
        Map.entry("api_gateway", List.of("nginx", "kong", "traefik", "envoy", "apache")),
        Map.entry("authentication_service", List.of("oauth2", "keycloak", "auth0", "okta", "jwt")),
        Map.entry("file_storage_handler", List.of("s3", "azure-blob", "gcs", "minio", "nfs")),
        Map.entry("payment_processor", List.of("stripe", "paypal", "braintree", "adyen", "square")),
        Map.entry("notification_service", List.of("smtp", "sendgrid", "ses", "twilio", "firebase")),
        Map.entry("search_indexer", List.of("elasticsearch", "solr", "opensearch", "algolia", "meilisearch")),
        Map.entry("session_manager", List.of("redis", "memcached", "hazelcast", "infinispan", "jwt")),
        Map.entry("metrics_collector", List.of("prometheus", "grafana", "datadog", "newrelic", "dynatrace")),
        Map.entry("load_balancer", List.of("nginx", "haproxy", "traefik", "aws-elb", "azure-lb")),
        Map.entry("container_orchestrator", List.of("kubernetes", "docker-swarm", "nomad", "ecs", "aks")),
        Map.entry("service_mesh", List.of("istio", "linkerd", "consul", "envoy", "kuma")),
        Map.entry("logging_aggregator", List.of("elasticsearch", "splunk", "datadog", "loki", "fluentd"))
    );

    @Inject
    FailureGeneratorAgent agent;

    // Emulates a log flow on each 1.3s
    @Scheduled(every = "1.5s")
    void generateChaos() {
        // Randomly select a component
        List<String> components = List.copyOf(COMPONENT_TECH_MAP.keySet());
        String component = components.get(random.nextInt(components.size()));
        
        // Get the associated technologies for this component
        List<String> technologies = COMPONENT_TECH_MAP.get(component);
        String tech = technologies.get(random.nextInt(technologies.size()));
        
        // Asks AI to create a realistic error log
        String logEntry = agent.generateErrorLog(1L, component, tech);

        // IRL, this could be sent to stdout or even Kafka
        LOG.error(logEntry);

        // Analyze the log and broadcast to UI
        try {
            String correlationId = java.util.UUID.randomUUID().toString();
            LogAnalysisResult analysis = analyzer.analyze(logEntry, correlationId);
            // Create a new result with the original log included
            LogAnalysisResult resultWithLog = new LogAnalysisResult(
                analysis.severity(),
                analysis.component(),
                analysis.errorType(),
                analysis.rootCauseSummary(),
                analysis.suggestedAction(),
                analysis.timestamp(),
                logEntry  // Add the original log
            );
            LogAnalysisWebSocket.broadcast(resultWithLog);
        } catch (Exception e) {
            LOG.errorf("Failed to analyze log: %s", e.getMessage());
        }
    }
}