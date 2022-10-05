package com.techcourse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nextstep.jdbc.JdbcTemplate;

public class TestDataUtils {

    private static final Logger log = LoggerFactory.getLogger(TestDataUtils.class);

    private TestDataUtils() {
    }

    public static void h2TruncateTables(final JdbcTemplate jdbcTemplate, final String... tableNames) {
        for (final String tableName : tableNames) {
            jdbcTemplate.update("truncate table " + tableName);
            jdbcTemplate.update("alter table " + tableName + " alter column id restart with 1");
            log.debug("table {} truncated", tableName);
        }
    }
}
