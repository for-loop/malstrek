package org.studioapriori.malstrek;

public class IntegerStringParser implements Parser<String, Integer> {
    public Integer parse(String integerString) {
        if (integerString == null || integerString.trim().isEmpty())
            return null;

        try {
            return Integer.parseInt(integerString);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
