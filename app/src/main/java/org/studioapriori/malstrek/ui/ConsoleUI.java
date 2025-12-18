package org.studioapriori.malstrek.ui;

import java.util.Scanner;

/**
 * Handles all console input/output operations.
 * Separated from business logic for testability and reusability.
 */
public class ConsoleUI {
    private final Scanner scanner;

    public ConsoleUI() {
        this.scanner = new Scanner(System.in);
    }

    public void displayGreeting(String message) {
        System.out.println(message);
    }

    public int promptForRaceNumber() {
        System.out.println("Enter race number:");
        String input = scanner.nextLine();
        return Integer.parseInt(input);
    }

    public String promptForStarterMode() {
        System.out.println("Enter 'start' to start with current timestamp. 'cont' to use existing timestamp (type 'exit' to quit):");
        return scanner.nextLine();
    }

    public int promptForBibNumber() {
        System.out.println("Enter finisher bib number (or type 'exit' to quit):");
        return Integer.parseInt(scanner.nextLine());
    }

    public String readInput() {
        return scanner.nextLine();
    }

    public void displayTimestamp(String label, long timestamp) {
        System.out.printf("Using %s timestamp: %d%n", label, timestamp);
    }

    public void displayMessage(String message) {
        System.out.println(message);
    }

    public void displayEvent(String topic, int raceNumber, long timestamp, String jsonString) {
        System.out.printf("Produced event to topic %s: %s %d %s%n", topic, raceNumber, timestamp, jsonString);
    }

    public void displayError(String message) {
        System.err.println("Error: " + message);
    }

    public void close() {
        scanner.close();
    }
}
