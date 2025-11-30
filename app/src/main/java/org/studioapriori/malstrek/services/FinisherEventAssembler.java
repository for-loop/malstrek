package org.studioapriori.malstrek.services;

import java.util.Arrays;
import org.studioapriori.malstrek.model.telemetry.FieldDefinition;
import org.studioapriori.malstrek.model.telemetry.FinisherData;
import org.studioapriori.malstrek.model.telemetry.Payload;
import org.studioapriori.malstrek.model.telemetry.PayloadWithDestination;
import org.studioapriori.malstrek.model.telemetry.Schema;
import org.studioapriori.malstrek.model.telemetry.TelemetryData;

public class FinisherEventAssembler extends EventAssembler {
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

    public FinisherEventAssembler(String topic, int race_number, Integer bib_number, long timestamp) {
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
