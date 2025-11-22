package org.studioapriori.malstrek.model.telemetry;

public record StarterData(
    boolean deleted,
    int race_number,
    long timestamp
) implements TelemetryData {}
