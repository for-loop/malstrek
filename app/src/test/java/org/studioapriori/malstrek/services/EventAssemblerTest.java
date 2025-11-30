package org.studioapriori.malstrek.services;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.studioapriori.malstrek.model.telemetry.PayloadWithDestination;
import static org.studioapriori.malstrek.Constants.*;

public abstract class EventAssemblerTest {

    protected final String TOPIC = "topic";
    protected final int RACE_NUMBER = 123;
    protected final long TIMESTAMP = 123456L;
    protected final int INVALID_RACE_NUMBER = -1;
    protected final int RACE_NUMBER_ZERO = 0;

    // Subclasses implement this factory method with their own constructor signature
    protected abstract EventAssembler createAssembler(String topic, int raceNumber, long timestamp);

    @Test void testCtrThrowsWhenTopicIsNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            createAssembler(null, RACE_NUMBER, TIMESTAMP);
        });
    }

    @Test void testCtrThrowsWhenRaceNumberIsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> {
            createAssembler(TOPIC, INVALID_RACE_NUMBER, TIMESTAMP);
        });
    }

    @Test void testCtrDoesNotThrowWhenRaceNumberIsZero() {
        assertDoesNotThrow(() -> createAssembler(TOPIC, RACE_NUMBER_ZERO, TIMESTAMP));
    }

    @ParameterizedTest
    @ValueSource(strings = {EMPTY_STRING, DOUBLE_WHITESPACE_STRING, NEWLINE_STRING, TAB_STRING})
    void testCtrThrowsWhenTopicIsEmptyOrWhiteSpaces(String testTopic) {
        assertThrows(IllegalArgumentException.class, () -> {
            createAssembler(testTopic, RACE_NUMBER, TIMESTAMP);
        });
    }

    @Test void testAssembleReturnsTwoPayloadsWithEqualValuesButDifferentReferences() {
        PayloadWithDestination payload1 = createAssembler(TOPIC, RACE_NUMBER, TIMESTAMP).assemble();
        PayloadWithDestination payload2 = createAssembler(TOPIC, RACE_NUMBER, TIMESTAMP).assemble();

        assertEquals(payload1, payload2);
        assertNotSame(payload1, payload2);
    }
}