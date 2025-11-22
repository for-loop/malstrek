package org.studioapriori.malstrek.model.telemetry;

import java.util.List;

public record Schema(
    boolean optional,
    String type,
    List<FieldDefinition> fields,
    int version
) {}
