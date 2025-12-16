package org.studioapriori.malstrek.util.parser;

/**
 * Parses a string into an Integer.
 * Returns null for empty/null strings or parse errors.
 */
public class IntegerStringParser implements Parser<String, Integer> {
    public Integer parse(String integerString) {
        if (integerString == null || integerString.trim().isEmpty())
            return null;

        try {
            return Integer.parseInt(integerString.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
