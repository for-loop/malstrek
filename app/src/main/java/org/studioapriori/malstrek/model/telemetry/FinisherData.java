package org.studioapriori.malstrek.model.telemetry;

public record FinisherData(
    boolean deleted,
    int race_number,
    Integer bib_number,
    long timestamp
) implements TelemetryData {}
