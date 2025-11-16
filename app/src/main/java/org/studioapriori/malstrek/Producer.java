package org.studioapriori.malstrek;

import org.json.JSONObject;

public class Producer {
    public Object parseStringInteger(String integerString) {
        if (integerString == null || integerString.trim().isEmpty())
            return JSONObject.NULL;
        
        try {
            return Integer.parseInt(integerString);
        } catch (NumberFormatException e) {
            return JSONObject.NULL;
        }
    }
}
