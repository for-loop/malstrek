package org.studioapriori.malstrek.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.studioapriori.malstrek.model.telemetry.PayloadWithDestination;
import org.studioapriori.malstrek.model.telemetry.StarterData;

public class StarterEventAssemblerTest extends EventAssemblerTest {

    protected EventAssembler createAssembler(String topic, int raceNumber, long timestamp) {
        return new StarterEventAssembler(topic, raceNumber, timestamp);
    }

    @Test
    void testAssembleReturnsPayloadWithDestination() {
        EventAssembler assembler = createAssembler(TOPIC, RACE_NUMBER, TIMESTAMP);

        PayloadWithDestination actual = assembler.assemble();
        StarterData data = (StarterData) actual.payload().payload();

        assertEquals(TOPIC, actual.topic());
        assertEquals(RACE_NUMBER, data.race_number());
        assertEquals(TIMESTAMP, data.timestamp());
    }
}
