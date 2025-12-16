package org.studioapriori.malstrek;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import static org.studioapriori.malstrek.Constants.*;

public class MultiStringParserTest {
    
    @Test void parseStringListReturnsEmptyListWhenGivenNull() {
        Parser<String, List<String>> classUnderTest = new MultiStringParser();
        List<String> actual = classUnderTest.parse(null);
        assert actual.isEmpty() : "parseStringList should return empty list when given null";
    }

    @ParameterizedTest
    @ValueSource(strings = {EMPTY_STRING, DOUBLE_WHITESPACE_STRING, NEWLINE_STRING, TAB_STRING})
    void parseStringListReturnsEmptyListWhenGivenEmptyStringOrWhiteSpaces(String testString) {
        Parser<String, List<String>> classUnderTest = new MultiStringParser();
        List<String> actual = classUnderTest.parse(testString);
        assert actual.isEmpty() : "parseStringList should return empty list when given empty string or whitespace";
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "one",
            DOUBLE_WHITESPACE_STRING + "singleWord" + DOUBLE_WHITESPACE_STRING,
            DOUBLE_WHITESPACE_STRING + "spacedOut" + DOUBLE_WHITESPACE_STRING
    })
    void parseStringListReturnsSingleElementListWhenGivenSingleWordString(String testString) {
        String trimmedString = testString.trim();
        Parser<String, List<String>> classUnderTest = new MultiStringParser();
        List<String> actual = classUnderTest.parse(testString);
        List<String> expected = List.of(trimmedString);
        assert actual.equals(expected) : "parseStringList should return list " + expected + " when given single word string " + testString;
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "apple banana cherry",
            DOUBLE_WHITESPACE_STRING + "apple banana cherry" + DOUBLE_WHITESPACE_STRING,
            DOUBLE_WHITESPACE_STRING + "apple banana" + DOUBLE_WHITESPACE_STRING + "cherry" + DOUBLE_WHITESPACE_STRING
    })
    void parseStringListReturnsListOfStringsWhenGivenValidString(String testString) {
        Parser<String, List<String>> classUnderTest = new MultiStringParser();
        List<String> actual = classUnderTest.parse(testString);
        List<String> expected = List.of("apple", "banana", "cherry");
        assert actual.equals(expected) : "parseStringList should return list " + expected + " when given string " + testString;
    }

    @Test void parseStringListHandlesCustomDelimiter() {
        String testString = "red,green,blue";
        String delimiter = ",";
        Parser<String, List<String>> classUnderTest = new MultiStringParser(delimiter);
        List<String> actual = classUnderTest.parse(testString);
        List<String> expected = List.of("red", "green", "blue");
        assert actual.equals(expected) : "parseStringList should return list " + expected + " when given string " + testString + " with delimiter " + delimiter;
    }
}
