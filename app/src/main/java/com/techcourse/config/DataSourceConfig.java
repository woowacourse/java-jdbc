package com.techcourse.config;

import org.h2.jdbcx.JdbcDataSource;

public class DataSourceConfig {

    private static final javax.sql.DataSource INSTANCE = createJdbcDataSource();

    public static javax.sql.DataSource getInstance() {
        return INSTANCE;
    }

    private static JdbcDataSource createJdbcDataSource() {
        final var jdbcDataSource = new JdbcDataSource();
        jdbcDataSource.setUrl(System.getProperty("jdbc.url", "jdbc:h2:file:./data/dashboard"));
        jdbcDataSource.setUser("");
        jdbcDataSource.setPassword("");
        return jdbcDataSource;
    }

    private DataSourceConfig() {}
}
