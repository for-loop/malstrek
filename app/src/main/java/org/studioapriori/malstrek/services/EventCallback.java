package org.studioapriori.malstrek.services;

import org.studioapriori.malstrek.model.RaceEvent;

/**
 * Callback interface for handling Kafka event production results.
 * Supports both success and error scenarios.
 */
@FunctionalInterface
public interface EventCallback {
    /**
     * Called when an event is successfully sent to Kafka.
     *
     * @param event the race event that was sent
     */
    void onSuccess(RaceEvent event);

    /**
     * Called when an error occurs while sending an event.
     * Default implementation prints the stack trace.
     *
     * @param ex the exception that occurred
     */
    default void onError(Exception ex) {
        ex.printStackTrace();
    }
}
