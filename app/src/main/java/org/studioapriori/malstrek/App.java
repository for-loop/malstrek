package org.studioapriori.malstrek;

import org.studioapriori.malstrek.controller.RaceEventController;
import org.studioapriori.malstrek.services.EventAssemblyService;
import org.studioapriori.malstrek.services.RaceEventProducer;
import org.studioapriori.malstrek.ui.ConsoleUI;

/**
 * Entry point for the Race Event application.
 * Orchestrates initialization and execution of the race event system.
 */
public class App {
    public static void main(String[] args) {
        String kafkaBootstrapServers = System.getenv("KAFKA_BOOTSTRAP_SERVERS");
        if (kafkaBootstrapServers == null || kafkaBootstrapServers.isBlank()) {
            throw new IllegalStateException(
                "KAFKA_BOOTSTRAP_SERVERS environment variable is not set. " +
                "Please set it to your Kafka broker address (e.g., localhost:9092 or broker.example.com:9092)"
            );
        }
        ConsoleUI ui = new ConsoleUI();
        ui.displayGreeting("Hello World!");

        RaceEventProducer producer = RaceEventProducer.create(kafkaBootstrapServers);
        EventAssemblyService assemblyService = new EventAssemblyService();
        RaceEventController controller = new RaceEventController(ui, producer, assemblyService);

        try {
            controller.run();
        } finally {
            producer.close();
            ui.close();
        }
    }
}