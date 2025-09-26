package com.interface21.jdbc.dsl;

public class Update {

    private final DslJdbcTemplate dslJdbcTemplate;

    private final String tableName;
    private final Set set = new Set();
    private final Where where = new Where();

    public Update(DslJdbcTemplate dslJdbcTemplate, String tableName) {
        this.dslJdbcTemplate = dslJdbcTemplate;
        this.tableName = tableName;
    }

    public Update set(String key, Object value) {
        set.add(key, value);
        return this;
    }

    public Update where(String key, Object value) {
        where.add(key, value);
        return this;
    }

    public void execute() {
        dslJdbcTemplate.update(this);
    }

    @Override
    public String toString() {
        return "UPDATE %s %s %s".formatted(tableName, set, where);
    }
}
