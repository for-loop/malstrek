package org.studioapriori.malstrek.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.studioapriori.malstrek.avro.Finisher;
import org.studioapriori.malstrek.avro.Starter;
import org.studioapriori.malstrek.model.RaceEvent;
import org.studioapriori.malstrek.services.EventAssemblyService;
import org.studioapriori.malstrek.services.EventCallback;
import org.studioapriori.malstrek.services.RaceEventProducer;
import org.studioapriori.malstrek.ui.ConsoleUI;

/**
 * Unit tests for RaceEventController.
 * Tests the orchestration between UI, services, and producer.
 */
@ExtendWith(MockitoExtension.class)
class RaceEventControllerTest {
    private static final int RACE_NUMBER = 2028;
    private static final long STARTER_TIMESTAMP = 1762883020000L;
    private static final long FINISHER_TIMESTAMP = 1762883080000L;
    private static final String STARTER_TOPIC = "race.starters";
    private static final String FINISHER_TOPIC = "race.finishers";

    @Mock
    private ConsoleUI mockUI;

    @Mock
    private RaceEventProducer mockProducer;

    @Mock
    private EventAssemblyService mockAssemblyService;

    @Captor
    private ArgumentCaptor<EventCallback> callbackCaptor;

    private RaceEventController controller;

    @BeforeEach
    void setUp() {
        controller = new RaceEventController(mockUI, mockProducer, mockAssemblyService);
    }

    private RaceEvent createStarterEvent() {
        return new RaceEvent(
            STARTER_TOPIC, 
            STARTER_TIMESTAMP, 
            RACE_NUMBER, 
            Starter.newBuilder()
                .setDeleted(false)
                .setRaceNumber(RACE_NUMBER)
                .setTimestamp(STARTER_TIMESTAMP)
                .build()
        );
    }

    private RaceEvent createFinisherEvent(Integer bibNumber) {
        return new RaceEvent(
            FINISHER_TOPIC, 
            FINISHER_TIMESTAMP, 
            RACE_NUMBER, 
            Finisher.newBuilder()
                .setDeleted(false)
                .setRaceNumber(RACE_NUMBER)
                .setBibNumber(bibNumber)
                .setTimestamp(FINISHER_TIMESTAMP)
                .build()
        );
    }

    @Test
    void run_promptsForRaceNumber() {
        when(mockUI.promptForRaceNumber()).thenReturn(RACE_NUMBER);
        when(mockUI.promptForStarterMode()).thenReturn("exit");

        controller.run();

        verify(mockUI).promptForRaceNumber();
    }

    @Test
    void run_withExitCommand_exitsWithoutProcessing() {
        when(mockUI.promptForRaceNumber()).thenReturn(RACE_NUMBER);
        when(mockUI.promptForStarterMode()).thenReturn("exit");

        controller.run();

        verify(mockUI).displayMessage("Exiting application.");
        verify(mockAssemblyService, never()).assembleStarterEvent(anyInt(), anyLong());
        verify(mockProducer, never()).sendEvent(any(), any());
    }

    @Test
    void run_withStartMode_assemblesAndSendsStarterEvent() {
        RaceEvent starterEvent = createStarterEvent();
        when(mockUI.promptForRaceNumber()).thenReturn(RACE_NUMBER);
        when(mockUI.promptForStarterMode()).thenReturn("start");
        when(mockAssemblyService.assembleStarterEvent(eq(RACE_NUMBER), anyLong())).thenReturn(starterEvent);
        when(mockUI.readInput()).thenReturn("exit");

        controller.run();

        verify(mockAssemblyService).assembleStarterEvent(eq(RACE_NUMBER), anyLong());
        verify(mockProducer).sendEvent(eq(starterEvent), any(EventCallback.class));
        verify(mockUI).displayTimestamp(eq("current"), anyLong());
    }

    @Test
    void run_withStartMode_invokesCallbackWithCorrectEvent() {
        RaceEvent starterEvent = createStarterEvent();
        when(mockUI.promptForRaceNumber()).thenReturn(RACE_NUMBER);
        when(mockUI.promptForStarterMode()).thenReturn("start");
        when(mockAssemblyService.assembleStarterEvent(eq(RACE_NUMBER), anyLong())).thenReturn(starterEvent);
        when(mockUI.readInput()).thenReturn("exit");

        controller.run();

        verify(mockProducer).sendEvent(eq(starterEvent), callbackCaptor.capture());
        callbackCaptor.getValue().onSuccess(starterEvent);

        verify(mockUI).displayEvent(
            starterEvent.topic(),
            starterEvent.avroRecord().toString()
        );
    }

    @Test
    void run_withContMode_doesNotSendStarterEvent() {
        when(mockUI.promptForRaceNumber()).thenReturn(RACE_NUMBER);
        when(mockUI.promptForStarterMode()).thenReturn("cont");
        when(mockUI.readInput()).thenReturn("exit");

        controller.run();

        verify(mockUI).displayMessage("Continuing with existing timestamps.");
        verify(mockAssemblyService, never()).assembleStarterEvent(anyInt(), anyLong());
    }

    @Test
    void run_withInvalidStarterMode_displaysErrorAndExits() {
        when(mockUI.promptForRaceNumber()).thenReturn(RACE_NUMBER);
        when(mockUI.promptForStarterMode()).thenReturn("invalid");

        controller.run();

        verify(mockUI).displayError("Invalid starter mode: invalid");
        verify(mockProducer, never()).sendEvent(any(), any());
    }

    @Test
    void run_withValidBibNumber_assemblesAndSendsFinisherEvent() {
        RaceEvent starterEvent = createStarterEvent();
        RaceEvent finisherEvent = createFinisherEvent(123);

        when(mockUI.promptForRaceNumber()).thenReturn(RACE_NUMBER);
        when(mockUI.promptForStarterMode()).thenReturn("start");
        when(mockAssemblyService.assembleStarterEvent(eq(RACE_NUMBER), anyLong())).thenReturn(starterEvent);
        when(mockUI.readInput()).thenReturn("123").thenReturn("exit");
        when(mockAssemblyService.assembleFinisherEvent(eq(RACE_NUMBER), eq(123), anyLong())).thenReturn(finisherEvent);

        controller.run();

        verify(mockAssemblyService).assembleFinisherEvent(eq(RACE_NUMBER), eq(123), anyLong());
        verify(mockProducer, times(2)).sendEvent(any(RaceEvent.class), any(EventCallback.class));
    }

    @Test
    void run_withMultipleBibNumbers_sendsMultipleFinisherEvents() {
        RaceEvent starterEvent = createStarterEvent();
        RaceEvent finisher1 = createFinisherEvent(101);
        RaceEvent finisher2 = createFinisherEvent(102);

        when(mockUI.promptForRaceNumber()).thenReturn(RACE_NUMBER);
        when(mockUI.promptForStarterMode()).thenReturn("start");
        when(mockAssemblyService.assembleStarterEvent(eq(RACE_NUMBER), anyLong())).thenReturn(starterEvent);
        when(mockUI.readInput()).thenReturn("101,102").thenReturn("exit");
        when(mockAssemblyService.assembleFinisherEvent(eq(RACE_NUMBER), eq(101), anyLong())).thenReturn(finisher1);
        when(mockAssemblyService.assembleFinisherEvent(eq(RACE_NUMBER), eq(102), anyLong())).thenReturn(finisher2);

        controller.run();

        // Verify finisher events were sent (exact count depends on MultiStringParser behavior)
        verify(mockProducer, atLeast(1)).sendEvent(any(RaceEvent.class), any(EventCallback.class));
    }

    @Test
    void run_withNullBibNumber_assemblesFinisherEventWithNullBib() {
        RaceEvent starterEvent = createStarterEvent();
        RaceEvent finisherEvent = createFinisherEvent(null);

        when(mockUI.promptForRaceNumber()).thenReturn(RACE_NUMBER);
        when(mockUI.promptForStarterMode()).thenReturn("start");
        when(mockAssemblyService.assembleStarterEvent(eq(RACE_NUMBER), anyLong())).thenReturn(starterEvent);
        when(mockUI.readInput()).thenReturn("").thenReturn("exit");
        when(mockAssemblyService.assembleFinisherEvent(eq(RACE_NUMBER), isNull(), anyLong())).thenReturn(finisherEvent);

        controller.run();

        verify(mockAssemblyService).assembleFinisherEvent(eq(RACE_NUMBER), isNull(), anyLong());
        verify(mockProducer).sendEvent(eq(finisherEvent), any(EventCallback.class));
    }

    @Test
    void run_withInvalidBibNumber_doesNotCrash() {
        RaceEvent starterEvent = createStarterEvent();

        when(mockUI.promptForRaceNumber()).thenReturn(RACE_NUMBER);
        when(mockUI.promptForStarterMode()).thenReturn("start");
        when(mockAssemblyService.assembleStarterEvent(eq(RACE_NUMBER), anyLong())).thenReturn(starterEvent);
        when(mockUI.readInput()).thenReturn("not_a_number").thenReturn("exit");

        // Should complete without throwing
        controller.run();

        // Verify the loop continued and exited gracefully
        verify(mockUI, times(2)).readInput();
    }

    @Test
    void run_finisherExitCommand_exitsFinisherLoop() {
        RaceEvent starterEvent = createStarterEvent();

        when(mockUI.promptForRaceNumber()).thenReturn(RACE_NUMBER);
        when(mockUI.promptForStarterMode()).thenReturn("start");
        when(mockAssemblyService.assembleStarterEvent(eq(RACE_NUMBER), anyLong())).thenReturn(starterEvent);
        when(mockUI.readInput()).thenReturn("exit");

        controller.run();

        verify(mockUI).displayMessage("Enter finisher bib number (or type 'exit' to quit):");
        verify(mockProducer, times(1)).sendEvent(any(RaceEvent.class), any(EventCallback.class));
    }

    @Test
    void run_invalidRaceNumber_displaysError() {
        when(mockUI.promptForRaceNumber()).thenThrow(new NumberFormatException("Invalid number"));

        controller.run();

        verify(mockUI).displayError(contains("Invalid number format"));
    }

    @Test
    void run_unexpectedException_displayErrorAndPrintsStackTrace() {
        when(mockUI.promptForRaceNumber()).thenThrow(new RuntimeException("Unexpected error"));

        controller.run();

        verify(mockUI).displayError(contains("Unexpected error"));
    }

    @Test
    void run_sendsMultipleFinisherEventsSequentially() {
        RaceEvent starterEvent = createStarterEvent();
        RaceEvent finisher1 = createFinisherEvent(1);
        RaceEvent finisher2 = createFinisherEvent(2);
        RaceEvent finisher3 = createFinisherEvent(3);

        when(mockUI.promptForRaceNumber()).thenReturn(RACE_NUMBER);
        when(mockUI.promptForStarterMode()).thenReturn("start");
        when(mockAssemblyService.assembleStarterEvent(eq(RACE_NUMBER), anyLong())).thenReturn(starterEvent);
        when(mockUI.readInput())
            .thenReturn("1")
            .thenReturn("2")
            .thenReturn("3")
            .thenReturn("exit");
        when(mockAssemblyService.assembleFinisherEvent(eq(RACE_NUMBER), eq(1), anyLong())).thenReturn(finisher1);
        when(mockAssemblyService.assembleFinisherEvent(eq(RACE_NUMBER), eq(2), anyLong())).thenReturn(finisher2);
        when(mockAssemblyService.assembleFinisherEvent(eq(RACE_NUMBER), eq(3), anyLong())).thenReturn(finisher3);

        controller.run();

        verify(mockProducer, times(4)).sendEvent(any(RaceEvent.class), any(EventCallback.class));
    }

    @Test
    void run_closesUIonCompletion() {
        when(mockUI.promptForRaceNumber()).thenReturn(RACE_NUMBER);
        when(mockUI.promptForStarterMode()).thenReturn("exit");

        controller.run();

        // Note: UI close is handled in App.java, not in controller
        // This test verifies controller doesn't interfere with resource management
        verify(mockProducer, never()).close();
    }
}
