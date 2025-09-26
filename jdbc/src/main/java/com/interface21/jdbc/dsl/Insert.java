package com.interface21.jdbc.dsl;

import java.util.Map;

public class Insert {

    private final DslJdbcTemplate dslJdbcTemplate;

    private final String tableName;
    private final ColumnNames columnNames = new ColumnNames();
    private final Values values = new Values();

    public Insert(DslJdbcTemplate dslJdbcTemplate, String tableName) {
        this.dslJdbcTemplate = dslJdbcTemplate;
        this.tableName = tableName;
    }

    public Insert of(Map<String, Object> map) {
        map.forEach((k, v) -> {
            columnNames.add(k);
            values.add(v);
        });
        return this;
    }

    public Insert of(String key, Object value) {
        return of(Map.of(key, value));
    }

    public void execute() {
        dslJdbcTemplate.insert(this);
    }

    @Override
    public String toString() {
        return "INSERT INTO %s (%s) %s".formatted(tableName, columnNames, values);
    }
}
