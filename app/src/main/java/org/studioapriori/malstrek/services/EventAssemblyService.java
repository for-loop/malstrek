package org.studioapriori.malstrek.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.studioapriori.malstrek.model.RaceEvent;
import org.studioapriori.malstrek.model.telemetry.PayloadWithDestination;

/**
 * Wraps event assembly and serialization logic.
 * Coordinates between assemblers and JSON serialization.
 */
public class EventAssemblyService {
    private final ObjectMapper objectMapper;

    public EventAssemblyService() {
        this.objectMapper = new ObjectMapper();
    }

    public RaceEvent assembleStarterEvent(int raceNumber, long timestamp) {
        PayloadWithDestination payload = new StarterEventAssembler(
            "start-line",
            raceNumber,
            timestamp
        ).assemble();

        String jsonString = serializePayload(payload);
        return new RaceEvent(
            payload.topic(),
            timestamp,
            raceNumber,
            jsonString
        );
    }

    public RaceEvent assembleFinisherEvent(int raceNumber, Integer bibNumber, long timestamp) {
        PayloadWithDestination payload = new FinisherEventAssembler(
            "finish-line",
            raceNumber,
            bibNumber,
            timestamp
        ).assemble();

        String jsonString = serializePayload(payload);
        return new RaceEvent(
            payload.topic(),
            timestamp,
            raceNumber,
            jsonString
        );
    }

    private String serializePayload(PayloadWithDestination payload) {
        try {
            return objectMapper.writeValueAsString(payload.payload());
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize payload", e);
        }
    }
}
