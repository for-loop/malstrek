package org.studioapriori.malstrek.model;
import org.apache.avro.specific.SpecificRecord;

/**
 * Data class representing a race event ready to send to Kafka.
 * Contains all information needed to produce an event to a Kafka topic.
 */
public record RaceEvent(
    String topic,
    long timestamp,
    int raceNumber,
    String jsonString,
    SpecificRecord avroRecord
) {
}
