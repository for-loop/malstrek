package org.studioapriori.malstrek;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.studioapriori.malstrek.Constants.*;

public class IntegerStringParserTest {
    
    @Test void parseStringIntegerReturnsJsonObjectNullWhenGivenNull() {
        Parser<String, Object> classUnderTest = new IntegerStringParser();
        assertEquals(JSONObject.NULL, classUnderTest.parse(null), "parseStringInteger should return JSONObject.NULL when given null");
    }

    @ParameterizedTest
    @ValueSource(strings = {EMPTY_STRING, DOUBLE_WHITESPACE_STRING, NEWLINE_STRING, TAB_STRING})
    void parseStringIntegerReturnsJsonObjectNullWhenGivenEmptyStringOrWhitespace(String integerString) {
        Parser<String, Object> classUnderTest = new IntegerStringParser();
        assertEquals(JSONObject.NULL, classUnderTest.parse(integerString), "parseStringInteger should return JSONObject.NULL when given empty string or whitespace");
    }

    @ParameterizedTest
    @ValueSource(strings = {"42", "-7", "0"})
    void parseStringIntegerReturnsIntegerWhenGivenValidIntegerString(String integerString) {
        int expected = Integer.parseInt(integerString);
        Parser<String, Object> classUnderTest = new IntegerStringParser();

        Object actual = classUnderTest.parse(integerString);

        assertEquals(expected, actual, "parseStringInteger should return Integer " + expected + " when given string " + integerString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "12.34", "123abc", DOUBLE_WHITESPACE_STRING + "one" + DOUBLE_WHITESPACE_STRING, "!"})
    void parseStringIntegerReturnsJsonObjectNullWhenGivenInvalidIntegerString(String integerString) {
        Parser<String, Object> classUnderTest = new IntegerStringParser();
        assertEquals(JSONObject.NULL, classUnderTest.parse(integerString), "parseStringInteger should return JSONObject.NULL when given non-integer string");
    }
}