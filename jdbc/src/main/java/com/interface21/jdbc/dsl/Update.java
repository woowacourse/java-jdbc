package com.interface21.jdbc.dsl;

import java.util.HashMap;
import java.util.Map;

public class Update {

    private final DslJdbcTemplate dslJdbcTemplate;

    private final String tableName;
    private final Map<String, Object> setMap = new HashMap<>();
    private final Map<String, Object> whereMap = new HashMap<>();

    public Update(DslJdbcTemplate dslJdbcTemplate, String tableName) {
        this.dslJdbcTemplate = dslJdbcTemplate;
        this.tableName = tableName;
    }

    public Update set(String key, Object value) {
        setMap.put(key, value);
        return this;
    }

    public Update where(String key, Object value) {
        whereMap.put(key, value);
        return this;
    }

    public void execute() {
        dslJdbcTemplate.update(this);
    }

    // 완성된 SQL의 형태를 구성한다.
    @Override
    public String toString() {
        String sets = setMap.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .reduce((a, b) -> a + ", " + b)
                .orElse("");

        String wheres = whereMap.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .reduce((a, b) -> a + " AND " + b)
                .orElse("");

        return "UPDATE %s SET %s WHERE %s".formatted(tableName, sets, wheres);
    }
}
