package org.studioapriori.malstrek;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

public class ProducerTest {
    @Test void parseStringIntegerReturnsJsonObjectNullWhenGivenNull() {
        Producer classUnderTest = new Producer();
        assertEquals(JSONObject.NULL, classUnderTest.parseStringInteger(null), "parseStringInteger should return JSONObject.NULL when given null");
    }

    @Test void parseStringIntegerReturnsJsonObjectNullWhenGivenEmptyStringOrWhitespace() {
        Producer classUnderTest = new Producer();
        assertEquals(JSONObject.NULL, classUnderTest.parseStringInteger(""), "parseStringInteger should return JSONObject.NULL when given empty string");
        assertEquals(JSONObject.NULL, classUnderTest.parseStringInteger("   "), "parseStringInteger should return JSONObject.NULL when given whitespace string");
    }

    @Test void parseStringIntegerReturnsIntegerWhenGivenValidIntegerString() {
        Producer classUnderTest = new Producer();
        assertEquals(42, classUnderTest.parseStringInteger("42"), "parseStringInteger should return Integer 42 when given string '42'");
        assertEquals(-7, classUnderTest.parseStringInteger("-7"), "parseStringInteger should return Integer -7 when given string '-7'");
    }

    @Test void parseStringIntegerReturnsJsonObjectNullWhenGivenInvalidIntegerString() {
        Producer classUnderTest = new Producer();
        assertEquals(JSONObject.NULL, classUnderTest.parseStringInteger("abc"), "parseStringInteger should return JSONObject.NULL when given non-integer string");
        assertEquals(JSONObject.NULL, classUnderTest.parseStringInteger("12.34"), "parseStringInteger should return JSONObject.NULL when given decimal string");
        assertEquals(JSONObject.NULL, classUnderTest.parseStringInteger("123abc"), "parseStringInteger should return JSONObject.NULL when given mixed string");
    }
}