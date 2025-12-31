package org.studioapriori.malstrek.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

import static org.studioapriori.malstrek.Constants.DOUBLE_WHITESPACE_STRING;

class AppConfigTest {
    private static final String KAFKA_BOOTSTRAP_SERVERS_ENV = "KAFKA_BOOTSTRAP_SERVERS";
    private static final String KAFKA_SCHEMA_REGISTRY_URL_ENV = "KAFKA_SCHEMA_REGISTRY_URL";
    private static final String LOCALHOST_KAFKA = "localhost:9092";
    private static final String LOCALHOST_SCHEMA_REGISTRY = "http://localhost:8081";
    private static final String MULTI_BROKER_KAFKA = "broker1.example.com:9092,broker2.example.com:9092";
    private static final String HTTPS_SCHEMA_REGISTRY = "https://schema-registry.example.com:8085";
    private static final String INTERNAL_DOMAIN_KAFKA = "kafka-test-01.internal:9092";
    private static final String INTERNAL_DOMAIN_SCHEMA_REGISTRY = "http://schema-reg-test.internal:8081";
    
    private AppConfig createAppConfig(Map<String, String> env) {
        return new AppConfig(env::get);
    }

    private Map<String, String> env;

    @BeforeEach
    void setUp() {
        env = new HashMap<>();
    }

    @Test
    void constructor_loadsValidEnvironmentVariables() {
        env.put(KAFKA_BOOTSTRAP_SERVERS_ENV, LOCALHOST_KAFKA);
        env.put(KAFKA_SCHEMA_REGISTRY_URL_ENV, LOCALHOST_SCHEMA_REGISTRY);

        AppConfig config = createAppConfig(env);

        assertEquals(LOCALHOST_KAFKA, config.getKafkaBootstrapServers());
        assertEquals(LOCALHOST_SCHEMA_REGISTRY, config.getSchemaRegistryUrl());
    }

    @Test
    void constructor_throwsException_whenKafkaBootstrapServersIsMissing() {
        env.put(KAFKA_SCHEMA_REGISTRY_URL_ENV, LOCALHOST_SCHEMA_REGISTRY);

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> createAppConfig(env)
        );

        assertTrue(exception.getMessage().contains(KAFKA_BOOTSTRAP_SERVERS_ENV));
    }

    @Test
    void constructor_throwsException_whenSchemaRegistryUrlIsMissing() {
        env.put(KAFKA_BOOTSTRAP_SERVERS_ENV, LOCALHOST_KAFKA);

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> createAppConfig(env)
        );

        assertTrue(exception.getMessage().contains(KAFKA_SCHEMA_REGISTRY_URL_ENV));
    }

    @Test
    void constructor_throwsException_whenKafkaBootstrapServersIsBlank() {
        env.put(KAFKA_BOOTSTRAP_SERVERS_ENV, DOUBLE_WHITESPACE_STRING);
        env.put(KAFKA_SCHEMA_REGISTRY_URL_ENV, LOCALHOST_SCHEMA_REGISTRY);

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> createAppConfig(env)
        );

        assertTrue(exception.getMessage().contains(KAFKA_BOOTSTRAP_SERVERS_ENV));
    }

    @Test
    void constructor_throwsException_whenSchemaRegistryUrlIsBlank() {
        env.put(KAFKA_BOOTSTRAP_SERVERS_ENV, LOCALHOST_KAFKA);
        env.put(KAFKA_SCHEMA_REGISTRY_URL_ENV, DOUBLE_WHITESPACE_STRING);

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> createAppConfig(env)
        );

        assertTrue(exception.getMessage().contains(KAFKA_SCHEMA_REGISTRY_URL_ENV));
    }

    @Test
    void constructor_throwsException_whenBothEnvironmentVariablesAreMissing() {
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> createAppConfig(env)
        );

        // Should fail on the first required variable
        assertTrue(exception.getMessage().contains(KAFKA_BOOTSTRAP_SERVERS_ENV));
    }

    @Test
    void getKafkaBootstrapServers_returnsConfiguredValue() {
        env.put(KAFKA_BOOTSTRAP_SERVERS_ENV, MULTI_BROKER_KAFKA);
        env.put(KAFKA_SCHEMA_REGISTRY_URL_ENV, "http://schema-registry:8081");

        AppConfig config = createAppConfig(env);

        assertEquals(MULTI_BROKER_KAFKA, config.getKafkaBootstrapServers());
    }

    @Test
    void getSchemaRegistryUrl_returnsConfiguredValue() {
        env.put(KAFKA_BOOTSTRAP_SERVERS_ENV, LOCALHOST_KAFKA);
        env.put(KAFKA_SCHEMA_REGISTRY_URL_ENV, HTTPS_SCHEMA_REGISTRY);

        AppConfig config = createAppConfig(env);

        assertEquals(HTTPS_SCHEMA_REGISTRY, config.getSchemaRegistryUrl());
    }

    @Test
    void constructor_handlesMixedCaseAndSpecialCharacters() {
        env.put(KAFKA_BOOTSTRAP_SERVERS_ENV, INTERNAL_DOMAIN_KAFKA);
        env.put(KAFKA_SCHEMA_REGISTRY_URL_ENV, INTERNAL_DOMAIN_SCHEMA_REGISTRY);

        AppConfig config = createAppConfig(env);

        assertEquals(INTERNAL_DOMAIN_KAFKA, config.getKafkaBootstrapServers());
        assertEquals(INTERNAL_DOMAIN_SCHEMA_REGISTRY, config.getSchemaRegistryUrl());
    }
}
