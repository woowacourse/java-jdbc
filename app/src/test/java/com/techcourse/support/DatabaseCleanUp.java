package com.techcourse.support;

import nextstep.jdbc.core.JdbcTemplate;

public class DatabaseCleanUp {

    public static void cleanUp(final JdbcTemplate jdbcTemplate, final String tableName) {
        truncateTable(jdbcTemplate, tableName);
        resetSequence(jdbcTemplate, tableName);
    }

    private static void truncateTable(JdbcTemplate jdbcTemplate, String tableName) {
        final String truncateSql = "truncate table " + tableName;
        jdbcTemplate.update(truncateSql);
    }

    private static void resetSequence(JdbcTemplate jdbcTemplate, String tableName) {
        final String alterSql = "alter table " + tableName + " alter column id restart with 1";
        jdbcTemplate.update(alterSql);
    }
}
