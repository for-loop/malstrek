package org.studioapriori.malstrek.config;

import java.util.function.Function;

/**
 * Centralized configuration for the application.
 * Loads and validates environment variables and provides default values.
 */
public class AppConfig {
    private final String kafkaBootstrapServers;
    private final String schemaRegistryUrl;

    /**
     * Creates AppConfig using system environment variables.
     */
    public AppConfig() {
        this(System.getenv()::get);
    }

    /**
     * Creates AppConfig with a custom environment variable provider.
     * Used for testing to inject mock environment variables.
     *
     * @param envProvider function that provides environment variable values
     */
    AppConfig(Function<String, String> envProvider) {
        this.kafkaBootstrapServers = loadKafkaBootstrapServers(envProvider);
        this.schemaRegistryUrl = loadSchemaRegistryUrl(envProvider);
    }

    private String loadKafkaBootstrapServers(Function<String, String> envProvider) {
        String value = envProvider.apply("KAFKA_BOOTSTRAP_SERVERS");
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                "KAFKA_BOOTSTRAP_SERVERS environment variable is not set. " +
                "Please set it to your Kafka broker address (e.g., localhost:9092 or broker.example.com:9092)"
            );
        }
        return value;
    }

    private String loadSchemaRegistryUrl(Function<String, String> envProvider) {
        String value = envProvider.apply("KAFKA_SCHEMA_REGISTRY_URL");
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                "KAFKA_SCHEMA_REGISTRY_URL environment variable is not set. " +
                "Please set it to your Schema Registry address (e.g., http://localhost:8081 or http://schema-registry.example.com:8081)"
            );
        }
        return value;
    }

    public String getKafkaBootstrapServers() {
        return kafkaBootstrapServers;
    }

    public String getSchemaRegistryUrl() {
        return schemaRegistryUrl;
    }
}
