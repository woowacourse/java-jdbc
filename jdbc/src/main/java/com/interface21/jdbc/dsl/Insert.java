package com.interface21.jdbc.dsl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Insert {

    private final DslJdbcTemplate dslJdbcTemplate;

    private final String tableName;
    private final Map<String, Object> valueMap = new HashMap<>();

    public Insert(DslJdbcTemplate dslJdbcTemplate, String tableName) {
        this.dslJdbcTemplate = dslJdbcTemplate;
        this.tableName = tableName;
    }

    public Insert of(String key, Object value) {
        valueMap.put(key, value);
        return this;
    }

    public void execute() {
        dslJdbcTemplate.insert(this);
    }

    // 완성된 SQL의 형태를 구성한다.
    @Override
    public String toString() {
        List<String> columnNames = new ArrayList<>();
        List<String> columnValues = new ArrayList<>();
        valueMap.forEach((key, value) -> {
            columnNames.add(key);
            if (value instanceof String) {
                columnValues.add("'" + value + "'");
            } else {
                columnValues.add(value.toString());
            }
        });
        String columns = columnNames.stream().reduce((a, b) -> a + ", " + b).orElse("");
        String values = columnValues.stream().reduce((a, b) -> a + ", " + b).orElse("");
        return "INSERT INTO %s (%s) VALUES (%s)".formatted(tableName, columns, values);
    }
}
