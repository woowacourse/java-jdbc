package com.techcourse.config;

import org.h2.jdbcx.JdbcDataSource;

public class DataSourceConfig {

    public static javax.sql.DataSource getInstance() {
        return Holder.INSTANCE;
    }

    private static class Holder {
        private static final javax.sql.DataSource INSTANCE = createJdbcDataSource();
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
