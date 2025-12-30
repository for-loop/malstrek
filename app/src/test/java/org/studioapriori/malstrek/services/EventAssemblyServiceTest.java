package org.studioapriori.malstrek.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.studioapriori.malstrek.avro.Finisher;
import org.studioapriori.malstrek.avro.Starter;
import org.studioapriori.malstrek.model.RaceEvent;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EventAssemblyService.
 * Verifies that starter and finisher events are correctly assembled.
 */
class EventAssemblyServiceTest {
    private static final int RACE_NUMBER = 2028;
    private static final long TIMESTAMP = 1762883020000L;
    private static final Integer BIB_NUMBER = 42;

    private EventAssemblyService service;

    @BeforeEach
    void setUp() {
        service = new EventAssemblyService();
    }

    @Test
    void assembleStarterEvent_returnsRaceEventWithCorrectTopic() {
        RaceEvent event = service.assembleStarterEvent(RACE_NUMBER, TIMESTAMP);

        assertEquals("start-line", event.topic());
    }

    @Test
    void assembleStarterEvent_returnsRaceEventWithCorrectTimestamp() {
        RaceEvent event = service.assembleStarterEvent(RACE_NUMBER, TIMESTAMP);

        assertEquals(TIMESTAMP, event.timestamp());
        assertEquals(TIMESTAMP, ((Starter) event.avroRecord()).getTimestamp());
    }

    @Test
    void assembleStarterEvent_returnsRaceEventWithCorrectRaceNumber() {
        RaceEvent event = service.assembleStarterEvent(RACE_NUMBER, TIMESTAMP);

        assertEquals(RACE_NUMBER, ((Starter) event.avroRecord()).getRaceNumber());
    }

    @Test
    void assembleStarterEvent_avroRecordIsValidAndNotEmpty() {
        RaceEvent event = service.assembleStarterEvent(RACE_NUMBER, TIMESTAMP);

        // Verify Avro record is not null and not empty
        assertNotNull(event.avroRecord());
        assertFalse(event.avroRecord().toString().isEmpty());

        // Verify it's valid JSON by parsing it
        ObjectMapper mapper = new ObjectMapper();
        assertDoesNotThrow(() -> mapper.readTree(event.avroRecord().toString()));
    }

    @Test
    void assembleStarterEvent_multipleCallsProduceDifferentEvents() {
        long timestamp1 = TIMESTAMP;
        long timestamp2 = TIMESTAMP + 1000;

        RaceEvent event1 = service.assembleStarterEvent(RACE_NUMBER, timestamp1);
        RaceEvent event2 = service.assembleStarterEvent(RACE_NUMBER, timestamp2);

        // Events should have different timestamps
        assertNotEquals(event1.timestamp(), event2.timestamp());
        // But same topic and race number
        assertEquals(event1.topic(), event2.topic());
        assertEquals(((Starter) event1.avroRecord()).getRaceNumber(), ((Starter) event2.avroRecord()).getRaceNumber());
    }

    // Tests for assembleFinisherEvent

    @Test
    void assembleFinisherEvent_returnsRaceEventWithCorrectTopic() {
        RaceEvent event = service.assembleFinisherEvent(RACE_NUMBER, BIB_NUMBER, TIMESTAMP);

        assertEquals("finish-line", event.topic());
    }

    @Test
    void assembleFinisherEvent_returnsRaceEventWithCorrectTimestamp() {
        RaceEvent event = service.assembleFinisherEvent(RACE_NUMBER, BIB_NUMBER, TIMESTAMP);

        assertEquals(TIMESTAMP, event.timestamp());
        assertEquals(TIMESTAMP, ((Finisher) event.avroRecord()).getTimestamp());
    }

    @Test
    void assembleFinisherEvent_returnsRaceEventWithCorrectRaceNumber() {
        RaceEvent event = service.assembleFinisherEvent(RACE_NUMBER, BIB_NUMBER, TIMESTAMP);

        assertEquals(RACE_NUMBER, ((Finisher) event.avroRecord()).getRaceNumber());
    }

    @Test
    void assembleFinisherEvent_avroRecordIsValidAndNotEmpty() {
        RaceEvent event = service.assembleFinisherEvent(RACE_NUMBER, BIB_NUMBER, TIMESTAMP);

        // Verify Avro Record is not null and not empty
        assertNotNull(event.avroRecord());
        assertFalse(event.avroRecord().toString().isEmpty());

        // Verify it's valid JSON by parsing it
        ObjectMapper mapper = new ObjectMapper();
        assertDoesNotThrow(() -> mapper.readTree(event.avroRecord().toString()));
    }

    @Test
    void assembleFinisherEvent_multipleCallsProduceDifferentEvents() {
        long timestamp1 = TIMESTAMP;
        long timestamp2 = TIMESTAMP + 1000;

        RaceEvent event1 = service.assembleFinisherEvent(RACE_NUMBER, BIB_NUMBER, timestamp1);
        RaceEvent event2 = service.assembleFinisherEvent(RACE_NUMBER, BIB_NUMBER, timestamp2);

        // Events should have different timestamps
        assertNotEquals(event1.timestamp(), event2.timestamp());
        // But same topic and race number
        assertEquals(event1.topic(), event2.topic());
        assertEquals(((Finisher) event1.avroRecord()).getRaceNumber(), ((Finisher) event2.avroRecord()).getRaceNumber());
    }

    @Test
    void assembleFinisherEvent_withNullBibNumber_returnsValidRaceEvent() {
        RaceEvent event = service.assembleFinisherEvent(RACE_NUMBER, null, TIMESTAMP);

        // Should return a valid RaceEvent even with null bib number
        assertNotNull(event);
        assertEquals("finish-line", event.topic());
        assertEquals(TIMESTAMP, event.timestamp());
        assertEquals(RACE_NUMBER, ((Finisher) event.avroRecord()).getRaceNumber());
        assertNotNull(event.avroRecord());
        assertFalse(event.avroRecord().toString().isEmpty());
    }
}
