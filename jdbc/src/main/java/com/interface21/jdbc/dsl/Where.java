package com.interface21.jdbc.dsl;

import java.util.HashMap;
import java.util.Map;

public class Where {

    private final Map<String, Object> whereMap = new HashMap<>();

    public void add(String key, Object value) {
        whereMap.put(key, value);
    }

    public boolean isEmpty() {
        return whereMap.isEmpty();
    }

    @Override
    public String toString() {
        if (isEmpty()) return "";

        return " WHERE " + whereMap.entrySet().stream()
                .map(entry -> {
                    if (entry.getValue() instanceof String) {
                        return entry.getKey() + " = '" + entry.getValue() + "'";
                    } else {
                        return entry.getKey() + " = " + entry.getValue();
                    }
                })
                .reduce((a, b) -> a + " AND " + b)
                .orElse("");
    }
}
