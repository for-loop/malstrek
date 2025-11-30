package org.studioapriori.malstrek.model.telemetry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class StarterModelAssemblerTest extends ModelAssemblerTest {

    protected ModelAssembler createAssembler(String topic, int raceNumber, long timestamp) {
        return new StarterModelAssembler(topic, raceNumber, timestamp);
    }

    @Test
    void testAssembleReturnsPayloadWithDestination() {
        ModelAssembler assembler = createAssembler(TOPIC, RACE_NUMBER, TIMESTAMP);

        PayloadWithDestination actual = assembler.assemble();
        StarterData data = (StarterData) actual.payload().payload();

        assertEquals(TOPIC, actual.topic());
        assertEquals(RACE_NUMBER, data.race_number());
        assertEquals(TIMESTAMP, data.timestamp());
    }
}
