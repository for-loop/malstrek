package org.studioapriori.malstrek.controller;

import java.util.List;

import org.studioapriori.malstrek.util.parser.IntegerStringParser;
import org.studioapriori.malstrek.util.parser.MultiStringParser;
import org.studioapriori.malstrek.util.parser.Parser;
import org.studioapriori.malstrek.model.RaceEvent;
import org.studioapriori.malstrek.services.EventAssemblyService;
import org.studioapriori.malstrek.services.RaceEventProducer;
import org.studioapriori.malstrek.ui.ConsoleUI;

/**
 * Controls the race event flow.
 * Orchestrates between UI, services, and producer.
 */
public class RaceEventController {
    private final ConsoleUI ui;
    private final RaceEventProducer producer;
    private final EventAssemblyService assemblyService;
    private final Parser<String, List<String>> multiStringParser;
    private final Parser<String, Integer> bibParser;

    public RaceEventController(ConsoleUI ui, RaceEventProducer producer, EventAssemblyService assemblyService) {
        this.ui = ui;
        this.producer = producer;
        this.assemblyService = assemblyService;
        this.multiStringParser = new MultiStringParser();
        this.bibParser = new IntegerStringParser();
    }

    public void run() {
        try {
            int raceNumber = ui.promptForRaceNumber();
            
            String starterMode = ui.promptForStarterMode();
            validateAndProcessStarterMode(starterMode, raceNumber);

            processFinisherEvents(raceNumber);
        } catch (NumberFormatException e) {
            ui.displayError("Invalid number format: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            if (!"exit".equals(e.getMessage())) {
                ui.displayError(e.getMessage());
            }
        } catch (Exception e) {
            ui.displayError("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void validateAndProcessStarterMode(String starterMode, int raceNumber) {
        if (starterMode.equalsIgnoreCase("exit")) {
            ui.displayMessage("Exiting application.");
            throw new IllegalArgumentException("exit");
        } else if (starterMode.equalsIgnoreCase("start")) {
            long starterTimestamp = System.currentTimeMillis();
            ui.displayTimestamp("current", starterTimestamp);
            
            RaceEvent starterEvent = assemblyService.assembleStarterEvent(raceNumber, starterTimestamp);
            sendEvent(starterEvent);
        } else if (starterMode.equalsIgnoreCase("cont")) {
            ui.displayMessage("Continuing with existing timestamps.");
        } else {
            throw new IllegalArgumentException("Invalid starter mode: " + starterMode);
        }
    }

    private void processFinisherEvents(int raceNumber) {
        while (true) {
            ui.displayMessage("Enter finisher bib number (or type 'exit' to quit):");
            String bibInput = ui.readInput();
            
            if (bibInput.equalsIgnoreCase("exit")) {
                break;
            }

            try {
                long finisherTimestamp = System.currentTimeMillis();
                ui.displayTimestamp("finish", finisherTimestamp);
                
                processFinisherBibNumber(raceNumber, bibInput, finisherTimestamp);
            } catch (NumberFormatException e) {
                ui.displayError("Invalid bib number: " + bibInput);
            }
        }
    }

    private void processFinisherBibNumber(int raceNumber, String bibInput, long finisherTimestamp) {
        List<String> potentialBibNumberStrings = multiStringParser.parse(bibInput);
        
        if (potentialBibNumberStrings.isEmpty()) {
            RaceEvent finisherEvent = assemblyService.assembleFinisherEvent(raceNumber, null, finisherTimestamp);
            sendEvent(finisherEvent);
            return;
        }

        for (String singleBibInput : potentialBibNumberStrings) {
            Integer nullableBibNumber = bibParser.parse(singleBibInput);
            RaceEvent finisherEvent = assemblyService.assembleFinisherEvent(raceNumber, nullableBibNumber, finisherTimestamp);
            sendEvent(finisherEvent);
        }
    }

    private void sendEvent(RaceEvent event) {
        producer.sendEvent(
            event,
            (raceEvent) -> 
                ui.displayEvent(raceEvent.topic(), raceEvent.raceNumber(), raceEvent.timestamp(), raceEvent.avroRecord().toString())
        );
    }
}
