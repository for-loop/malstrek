package org.studioapriori.malstrek.util.parser;

import java.util.List;

/**
 * Parses a string into a list of strings by splitting on a delimiter.
 * Default delimiter is whitespace. Returns empty list for null/empty input.
 */
public class MultiStringParser implements Parser<String, List<String>> {
    private final String delimiter;

    public MultiStringParser() {
        this.delimiter = "\\s+";
    }

    public MultiStringParser(String delimiter) {
        this.delimiter = delimiter;
    }

    public List<String> parse(String input) {
        if (input == null || input.trim().isEmpty()) {
            return List.of();
        }
        String[] parts = input.trim().split(delimiter);
        return List.of(parts);
    }
}
