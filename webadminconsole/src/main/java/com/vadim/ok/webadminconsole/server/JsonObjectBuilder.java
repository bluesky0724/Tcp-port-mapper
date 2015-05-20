package com.vadim.ok.webadminconsole.server;

import java.util.*;

public class JsonObjectBuilder {
    private final Map<String, String> objectProperties = new HashMap<String, String>();

    public JsonObjectBuilder withProperty(String propertyName, String propertyValue) {
        objectProperties.put(propertyName, propertyValue);
        return this;
    }

    public String build() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");

        Iterator<Map.Entry<String, String>> iterator = objectProperties.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();

            stringBuilder.append("\"");
            stringBuilder.append(entry.getKey());
            stringBuilder.append("\":");
            stringBuilder.append("\"");
            stringBuilder.append(entry.getValue());
            stringBuilder.append("\"");
            if (iterator.hasNext()) {
                stringBuilder.append(",");
            }
        }

        stringBuilder.append("}");

        return stringBuilder.toString();
    }
}
