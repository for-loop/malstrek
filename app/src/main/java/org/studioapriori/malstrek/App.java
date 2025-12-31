package org.studioapriori.malstrek;

import org.studioapriori.malstrek.config.AppConfig;
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
        AppConfig config = new AppConfig();
        
        ConsoleUI ui = new ConsoleUI();
        ui.displayGreeting("Hello World!");

        RaceEventProducer producer = RaceEventProducer.create(config);
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