package com.techcourse.support;

import com.interface21.jdbc.core.JdbcTemplate;

public class DataBaseCleaner {

    private final JdbcTemplate jdbcTemplate;

    public DataBaseCleaner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void cleanUp(String tableName) {
        String truncateSql = String.format("TRUNCATE TABLE %s;", tableName);
        jdbcTemplate.update(truncateSql);

        String resetAutoIncrementSql = String.format("ALTER TABLE %s ALTER COLUMN ID RESTART WITH 1;", tableName);
        jdbcTemplate.update(resetAutoIncrementSql);
    }
}
