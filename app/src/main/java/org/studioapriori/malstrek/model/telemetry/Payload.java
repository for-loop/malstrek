package org.studioapriori.malstrek.model.telemetry;

public record Payload(
    Schema schema,
    TelemetryData payload
) {}
