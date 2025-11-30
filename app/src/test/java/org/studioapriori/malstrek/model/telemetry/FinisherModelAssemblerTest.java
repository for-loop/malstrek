package org.studioapriori.malstrek.model.telemetry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

public class FinisherModelAssemblerTest extends ModelAssemblerTest {

    private final Integer BIB_NUMBER = 456;

    protected ModelAssembler createAssembler(String topic, int raceNumber, long timestamp) {
        return new FinisherModelAssembler(topic, raceNumber, BIB_NUMBER, timestamp);
    }

    @Test void testAssembleReturnsNullBibNumberWhenBibNumberIsNull() {
        ModelAssembler assembler = new FinisherModelAssembler(
            TOPIC,
            RACE_NUMBER,
            null,
            TIMESTAMP
        );

        PayloadWithDestination actual = assembler.assemble();
        
        assertNull(((FinisherData) actual.payload().payload()).bib_number());
    }

    @Test void testAssembleReturnsPayloadWithDestination() {
        ModelAssembler assembler = createAssembler(TOPIC, RACE_NUMBER, TIMESTAMP);

        PayloadWithDestination actual = assembler.assemble();
        FinisherData data = (FinisherData) actual.payload().payload();

        assertEquals(TOPIC, actual.topic());
        assertEquals(RACE_NUMBER, data.race_number());
        assertEquals(BIB_NUMBER, data.bib_number());
        assertEquals(TIMESTAMP, data.timestamp());
    }

    @Test void testAssembleReturnsTwoPayloadsWithEqualValuesButDifferentReferences() {
        PayloadWithDestination payload1 = createAssembler(TOPIC, RACE_NUMBER, TIMESTAMP).assemble();

        PayloadWithDestination payload2 = createAssembler(TOPIC, RACE_NUMBER, TIMESTAMP).assemble();

        assertEquals(payload1, payload2, "PayloadWithDestination instances should have equal values.");
        assertNotSame(payload1, payload2, "PayloadWithDestination instances should have different references.");
    }
}
