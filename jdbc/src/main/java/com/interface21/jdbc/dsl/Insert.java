package com.interface21.jdbc.dsl;

public class Insert {

    private final DslJdbcTemplate dslJdbcTemplate;

    private final String tableName;
    private final ColumnNames columnNames = new ColumnNames();
    private final Values values = new Values();

    public Insert(DslJdbcTemplate dslJdbcTemplate, String tableName) {
        this.dslJdbcTemplate = dslJdbcTemplate;
        this.tableName = tableName;
    }

    public Insert of(String key, Object value) {
        columnNames.add(key);
        values.add(value);
        return this;
    }

    public void execute() {
        dslJdbcTemplate.insert(this);
    }

    // 완성된 SQL의 형태를 구성한다.
    @Override
    public String toString() {
        return "INSERT INTO %s (%s) %s".formatted(tableName, columnNames, values);
    }
}
