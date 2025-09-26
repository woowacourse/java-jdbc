package com.interface21.jdbc.dsl;

import java.util.HashMap;
import java.util.Map;

public class Set {

    private final Map<String, DbObject> setMap = new HashMap<>();

    public void add(String key, Object value) {
        setMap.put(key, new DbObject(value));
    }

    @Override
    public String toString() {
        return "SET " + setMap.entrySet().stream()
                .map(entry -> entry.getKey() + " = " + entry.getValue())
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }
}
