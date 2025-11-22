package org.studioapriori.malstrek.model.telemetry;

public record PayloadWithDestination(
    String topic,
    Payload payload
) {}
