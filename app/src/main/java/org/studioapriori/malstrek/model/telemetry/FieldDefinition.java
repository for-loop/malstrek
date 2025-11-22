package org.studioapriori.malstrek.model.telemetry;

public record FieldDefinition(
    String field,
    boolean optional,
    String type
) {}
