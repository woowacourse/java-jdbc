package com.techcourse.support;

import com.interface21.jdbc.core.JdbcTemplate;

public class DatabaseCleaner {

    private DatabaseCleaner() {}

    public static void clear(final JdbcTemplate jdbcTemplate) {
        jdbcTemplate.update("delete from user_history");
        jdbcTemplate.update("delete from users");
        jdbcTemplate.update("alter table user_history alter column id restart with 1");
        jdbcTemplate.update("alter table users alter column id restart with 1");
    }
}
