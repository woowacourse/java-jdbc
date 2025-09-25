package com.interface21.jdbc.dsl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Values {

    private final Map<String, DbObject> valueMap = new HashMap<>();

    void add(String key, Object value) {
        valueMap.put(key, new DbObject(value));
    }

    @Override
    public String toString() {
        List<String> columnNames = new ArrayList<>();
        List<String> columnValues = new ArrayList<>();
        valueMap.forEach((key, value) -> {
            columnNames.add(key);
            columnValues.add(value.toString());
        });
        String columns = columnNames.stream().reduce((a, b) -> a + ", " + b).orElse("");
        String values = columnValues.stream().reduce((a, b) -> a + ", " + b).orElse("");

        return " (%s) VALUES (%s)".formatted(columns, values);
    }
}
