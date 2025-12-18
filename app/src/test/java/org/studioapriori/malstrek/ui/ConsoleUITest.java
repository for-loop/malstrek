package org.studioapriori.malstrek.ui;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ConsoleUI.
 * Tests console input/output operations with redirected System.in/out/err.
 */
class ConsoleUITest {
    private static final String GREETING_MESSAGE = "Hello, Race Timing System!";
    private static final String PROMPT_RACE = "2028";
    private static final String STARTER_MODE_INPUT = "start";
    private static final String FINISHER_BIB = "42";
    private static final String ARBITRARY_INPUT = "some input";
    private static final String ERROR_MESSAGE = "Something went wrong";
    private static final String RACE_TOPIC = "race.starters";
    private static final int RACE_NUMBER = 2028;
    private static final long TIMESTAMP = 1762883020000L;
    private static final String JSON_DATA = "{\"type\":\"starter\"}";

    private ByteArrayOutputStream outContent;
    private ByteArrayOutputStream errContent;
    private PrintStream originalOut;
    private PrintStream originalErr;
    private ConsoleUI ui;

    @BeforeEach
    void setUp() {
        originalOut = System.out;
        originalErr = System.err;
        outContent = new ByteArrayOutputStream();
        errContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    void tearDown() {
        if (ui != null) {
            ui.close();
        }
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    private void setSystemIn(String input) {
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        ui = new ConsoleUI();
    }

    private void initUI() {
        System.setIn(new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)));
        ui = new ConsoleUI();
    }

    private String getSystemOut() {
        return outContent.toString(StandardCharsets.UTF_8);
    }

    private String getSystemErr() {
        return errContent.toString(StandardCharsets.UTF_8);
    }

    @Test
    void displayGreeting_printsMessageToStdout() {
        initUI();
        ui.displayGreeting(GREETING_MESSAGE);

        assertTrue(getSystemOut().contains(GREETING_MESSAGE));
    }

    @Test
    void promptForRaceNumber_printsPromptAndReturnsNumber() {
        setSystemIn(PROMPT_RACE);

        int result = ui.promptForRaceNumber();

        assertEquals(2028, result);
        assertTrue(getSystemOut().contains("Enter race number"));
    }

    @Test
    void promptForRaceNumber_withLeadingWhitespace_throwsNumberFormatException() {
        setSystemIn("  2028  ");

        // Integer.parseInt doesn't auto-trim whitespace
        assertThrows(NumberFormatException.class, ui::promptForRaceNumber);
    }

    @Test
    void promptForRaceNumber_withInvalidInput_throwsNumberFormatException() {
        setSystemIn("not_a_number");

        assertThrows(NumberFormatException.class, ui::promptForRaceNumber);
    }

    @Test
    void promptForStarterMode_printsPromptAndReturnsInput() {
        setSystemIn(STARTER_MODE_INPUT);

        String result = ui.promptForStarterMode();

        assertEquals("start", result);
        assertTrue(getSystemOut().contains("Enter 'start'"));
    }

    @Test
    void promptForStarterMode_returnsCasePreserved() {
        setSystemIn("START");

        String result = ui.promptForStarterMode();

        assertEquals("START", result);
    }

    @Test
    void promptForStarterMode_withExitCommand_returnsExit() {
        setSystemIn("exit");

        String result = ui.promptForStarterMode();

        assertEquals("exit", result);
    }

    @Test
    void readInput_returnsUserInput() {
        setSystemIn(ARBITRARY_INPUT);

        String result = ui.readInput();

        assertEquals(ARBITRARY_INPUT, result);
    }

    @Test
    void readInput_multipleCallsReturnMultipleInputs() {
        setSystemIn("first\nsecond\nthird");

        String first = ui.readInput();
        String second = ui.readInput();
        String third = ui.readInput();

        assertEquals("first", first);
        assertEquals("second", second);
        assertEquals("third", third);
    }

    @Test
    void readInput_withWhitespace_preservesWhitespace() {
        setSystemIn("  input with spaces  ");

        String result = ui.readInput();

        assertEquals("  input with spaces  ", result);
    }

    @Test
    void readInput_emptyLine_returnsEmptyString() {
        setSystemIn("\n");

        String result = ui.readInput();

        assertEquals("", result);
    }

    @Test
    void displayMessage_printsToStdout() {
        initUI();
        String message = "Processing finisher event...";

        ui.displayMessage(message);

        assertTrue(getSystemOut().contains(message));
    }

    @Test
    void displayTimestamp_formatsAndPrintsTimestamp() {
        initUI();
        ui.displayTimestamp("current", TIMESTAMP);

        String output = getSystemOut();
        assertTrue(output.contains("Using current timestamp"));
        assertTrue(output.contains(String.valueOf(TIMESTAMP)));
    }

    @Test
    void displayTimestamp_withFinishLabel_printsFinishTimestamp() {
        initUI();
        ui.displayTimestamp("finish", TIMESTAMP);

        assertTrue(getSystemOut().contains("Using finish timestamp"));
    }

    @Test
    void displayEvent_formatsAndPrintsEventData() {
        initUI();
        ui.displayEvent(RACE_TOPIC, RACE_NUMBER, TIMESTAMP, JSON_DATA);

        String output = getSystemOut();
        assertTrue(output.contains("Produced event to topic"));
        assertTrue(output.contains(RACE_TOPIC));
        assertTrue(output.contains(String.valueOf(RACE_NUMBER)));
        assertTrue(output.contains(JSON_DATA));
    }

    @Test
    void displayEvent_withSpecialCharactersInJson_handlesCorrectly() {
        initUI();
        String complexJson = "{\"field\":\"value\\nwith\\nnewlines\"}";

        ui.displayEvent("topic", 123, 456L, complexJson);

        assertTrue(getSystemOut().contains(complexJson));
    }

    @Test
    void displayError_printsToStderr() {
        initUI();
        ui.displayError(ERROR_MESSAGE);

        String output = getSystemErr();
        assertTrue(output.contains("Error:"));
        assertTrue(output.contains(ERROR_MESSAGE));
    }

    @Test
    void displayError_doesNotPrintToStdout() {
        initUI();
        ui.displayError(ERROR_MESSAGE);

        assertFalse(getSystemOut().contains(ERROR_MESSAGE));
    }

    @Test
    void displayError_withQuotes_handlesCorrectly() {
        initUI();
        String errorWithQuotes = "Expected \"value\" but got 'other'";

        ui.displayError(errorWithQuotes);

        assertTrue(getSystemErr().contains(errorWithQuotes));
    }

    @Test
    void close_preventsFurtherInput() {
        setSystemIn("test");
        
        ui.close();
        
        // After close, attempting to read should fail or return empty
        // This verifies the scanner is properly closed
        assertThrows(Exception.class, ui::readInput);
    }

    @Test
    void promptForBibNumber_parsesValidInput() {
        setSystemIn(FINISHER_BIB);

        int result = ui.promptForBibNumber();

        assertEquals(42, result);
    }

    @Test
    void promptForBibNumber_withInvalidInput_throwsNumberFormatException() {
        setSystemIn("abc");

        assertThrows(NumberFormatException.class, ui::promptForBibNumber);
    }

    @Test
    void multipleOperations_sequentiallyWorkCorrectly() {
        setSystemIn("2028\nstart\n");

        // Simulate controller flow
        ui.displayGreeting("Starting race event system");
        int raceNumber = ui.promptForRaceNumber();
        String starterMode = ui.promptForStarterMode();

        String output = getSystemOut();
        assertTrue(output.contains("Starting race event system"));
        assertEquals(2028, raceNumber);
        assertEquals("start", starterMode);
    }
}
