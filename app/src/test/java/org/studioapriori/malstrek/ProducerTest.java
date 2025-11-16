package org.studioapriori.malstrek;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class ProducerTest {
    private static final String EMPTY_STRING = "";
    private static final String DOUBLE_WHITESPACE_STRING = "  ";
    private static final String NEWLINE_STRING = "\n";
    private static final String TAB_STRING = "\t";

    @Test void parseStringIntegerReturnsJsonObjectNullWhenGivenNull() {
        Producer classUnderTest = new Producer();
        assertEquals(JSONObject.NULL, classUnderTest.parseStringInteger(null), "parseStringInteger should return JSONObject.NULL when given null");
    }

    @ParameterizedTest
    @ValueSource(strings = {EMPTY_STRING, DOUBLE_WHITESPACE_STRING, NEWLINE_STRING, TAB_STRING})
    void parseStringIntegerReturnsJsonObjectNullWhenGivenEmptyStringOrWhitespace(String integerString) {
        Producer classUnderTest = new Producer();
        assertEquals(JSONObject.NULL, classUnderTest.parseStringInteger(integerString), "parseStringInteger should return JSONObject.NULL when given empty string or whitespace");
    }

    @ParameterizedTest
    @ValueSource(strings = {"42", "-7", "0"})
    void parseStringIntegerReturnsIntegerWhenGivenValidIntegerString(String integerString) {
        int expected = Integer.parseInt(integerString);
        Producer classUnderTest = new Producer();

        Object actual = classUnderTest.parseStringInteger(integerString);

        assertEquals(expected, actual, "parseStringInteger should return Integer " + expected + " when given string " + integerString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "12.34", "123abc", DOUBLE_WHITESPACE_STRING + "one" + DOUBLE_WHITESPACE_STRING, "!"})
    void parseStringIntegerReturnsJsonObjectNullWhenGivenInvalidIntegerString(String integerString) {
        Producer classUnderTest = new Producer();
        assertEquals(JSONObject.NULL, classUnderTest.parseStringInteger(integerString), "parseStringInteger should return JSONObject.NULL when given non-integer string");
    }
}