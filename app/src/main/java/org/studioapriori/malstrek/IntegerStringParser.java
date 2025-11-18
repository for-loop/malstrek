package org.studioapriori.malstrek;

import org.json.JSONObject;

public class IntegerStringParser implements Parser<String, Object> {
    public Object parse(String integerString) {
        if (integerString == null || integerString.trim().isEmpty())
            return JSONObject.NULL;

        try {
            return Integer.parseInt(integerString);
        } catch (NumberFormatException e) {
            return JSONObject.NULL;
        }
    }
}
