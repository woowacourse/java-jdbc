package com.techcourse.config;

import org.h2.jdbcx.JdbcDataSource;

import java.util.Objects;

public class DataSourceConfig {

    private static javax.sql.DataSource INSTANCE;

    public static javax.sql.DataSource getInstance() {
        if (Objects.isNull(INSTANCE)) {
            INSTANCE = createJdbcDataSource();
        }
        return INSTANCE;
    }

    private static JdbcDataSource createJdbcDataSource() {
        final var jdbcDataSource = new JdbcDataSource();
        jdbcDataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;");
        jdbcDataSource.setUser("");
        jdbcDataSource.setPassword("");
        return jdbcDataSource;
    }

    private DataSourceConfig() {}
}
