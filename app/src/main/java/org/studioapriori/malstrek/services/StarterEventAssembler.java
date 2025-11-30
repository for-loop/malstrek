package org.studioapriori.malstrek.services;

import java.util.Arrays;
import org.studioapriori.malstrek.model.telemetry.FieldDefinition;
import org.studioapriori.malstrek.model.telemetry.Payload;
import org.studioapriori.malstrek.model.telemetry.PayloadWithDestination;
import org.studioapriori.malstrek.model.telemetry.Schema;
import org.studioapriori.malstrek.model.telemetry.StarterData;
import org.studioapriori.malstrek.model.telemetry.TelemetryData;

public class StarterEventAssembler extends EventAssembler {
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

    public StarterEventAssembler(String topic, int race_number, long timestamp) {
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
