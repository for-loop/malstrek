package org.studioapriori.malstrek.model.telemetry;

public abstract class ModelAssembler {
    public String topic;
    public int race_number;
    public long timestamp;

    public ModelAssembler(String topic, int race_number, long timestamp) {
        if (topic == null || topic.isBlank())
            throw new IllegalArgumentException("Topic cannot be null or blank.");
        
        if (race_number < 0)
            throw new IllegalArgumentException("Race number cannot be negative.");

        this.topic = topic;
        this.race_number = race_number;
        this.timestamp = timestamp;
    }

    public abstract PayloadWithDestination assemble();
}
