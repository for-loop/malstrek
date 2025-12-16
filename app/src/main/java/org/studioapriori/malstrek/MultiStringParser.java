package org.studioapriori.malstrek;

import java.util.List;

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
