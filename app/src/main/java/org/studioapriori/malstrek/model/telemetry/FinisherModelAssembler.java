package org.studioapriori.malstrek.model.telemetry;

import java.util.Arrays;

public class FinisherModelAssembler extends ModelAssembler {
    public Integer bib_number;

    public static final Schema FINISHER_SCHEMA = new Schema(
        false,
        "struct",
        Arrays.asList(
            new FieldDefinition("race_number", false, "int32"),
            new FieldDefinition("bib_number", true, "int32"),
            new FieldDefinition("timestamp", false, "int64"),
            new FieldDefinition("deleted", false, "boolean")
        ),
        1
    );

    public FinisherModelAssembler(String topic, int race_number, Integer bib_number, long timestamp) {
        super(topic, race_number, timestamp);
        this.bib_number = bib_number;
    }

    public PayloadWithDestination assemble() {
        TelemetryData data = new FinisherData(
            false,
            this.race_number,
            this.bib_number,
            this.timestamp
        );
        
        Payload payload = new Payload(
            FINISHER_SCHEMA,
            data
        );

        return new PayloadWithDestination (
            this.topic,
            payload
        );
    }
}
