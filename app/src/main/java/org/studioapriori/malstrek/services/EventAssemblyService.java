package org.studioapriori.malstrek.services;

import org.studioapriori.malstrek.model.RaceEvent;
import org.studioapriori.malstrek.avro.Starter;
import org.studioapriori.malstrek.avro.Finisher;

/**
 * Service responsible for assembling RaceEvent objects.
 * Creates starter and finisher events with appropriate Avro records.
 */
public class EventAssemblyService {

    public RaceEvent assembleStarterEvent(int raceNumber, long timestamp) {
        return new RaceEvent(
            "start-line",
            timestamp,
            raceNumber,
            Starter.newBuilder()
                .setRaceNumber(raceNumber)
                .setTimestamp(timestamp)
                .build()
        );
    }

    public RaceEvent assembleFinisherEvent(int raceNumber, Integer bibNumber, long timestamp) {
        return new RaceEvent(
            "finish-line",
            timestamp,
            raceNumber,
            Finisher.newBuilder()
                .setRaceNumber(raceNumber)
                .setBibNumber(bibNumber)
                .setTimestamp(timestamp)
                .build()
        );
    }
}
