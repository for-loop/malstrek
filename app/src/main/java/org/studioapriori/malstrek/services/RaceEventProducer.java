package org.studioapriori.malstrek.services;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.studioapriori.malstrek.model.RaceEvent;
import static org.apache.kafka.clients.producer.ProducerConfig.*;

import java.util.Properties;

/**
 * Wraps Kafka producer logic.
 * Handles event sending and producer lifecycle.
 */
public class RaceEventProducer {
    private final Producer<String, String> producer;

    /**
     * Creates a RaceEventProducer with the provided Kafka producer.
     * Used for dependency injection and testing.
     *
     * @param producer the Kafka producer instance
     */
    public RaceEventProducer(Producer<String, String> producer) {
        this.producer = producer;
    }

    /**
     * Factory method to create a RaceEventProducer with default configuration.
     * Used in production code.
     *
     * @param bootstrapServers the Kafka bootstrap servers address
     * @return a new RaceEventProducer instance
     */
    public static RaceEventProducer create(String bootstrapServers) {
        final Properties props = new Properties() {
            {
                put(BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
                put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getCanonicalName());
                put(VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getCanonicalName());
                put(ACKS_CONFIG, "all");
            }
        };
        return new RaceEventProducer(new KafkaProducer<>(props));
    }

    /**
     * Sends a race event to Kafka.
     *
     * @param event the race event to send
     * @param callback the callback to invoke on completion (success or error)
     */
    public void sendEvent(RaceEvent event, EventCallback callback) {
        ProducerRecord<String, String> record = new ProducerRecord<>(
            event.topic(),
            null,
            event.timestamp(),
            null,
            event.jsonString()
        );

        producer.send(record, (producerEvent, ex) -> {
            if (ex != null) {
                callback.onError(ex);
            } else {
                callback.onSuccess(event);
            }
        });
    }

    public void close() {
        producer.close();
    }
}
