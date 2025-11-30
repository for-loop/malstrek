package org.studioapriori.malstrek.model.telemetry;

import java.util.Arrays;

public class StarterModelAssembler extends ModelAssembler {
    public Integer bib_number;

    public static final Schema STARTER_SCHEMA = new Schema(
        false,
        "struct",
        Arrays.asList(
            new FieldDefinition("race_number", false, "int32"),
            new FieldDefinition("timestamp", false, "int64"),
            new FieldDefinition("deleted", false, "boolean")
        ),
        1
    );

    public StarterModelAssembler(String topic, int race_number, long timestamp) {
        super(topic, race_number, timestamp);
    }

    public PayloadWithDestination assemble() {
        TelemetryData data = new StarterData(
            false,
            this.race_number,
            this.timestamp
        );
        
        Payload payload = new Payload(
            STARTER_SCHEMA,
            data
        );

        return new PayloadWithDestination (
            this.topic,
            payload
        );
    }
}
