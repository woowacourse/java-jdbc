package com.techcourse.config;

import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;
import java.util.Objects;

public class DataSourceConfig {

    private static final DataSource INSTANCE = createDataSource(
            System.getProperty("jdbc.url", "jdbc:h2:file:./data/java-jdbc"));

    public static DataSource getInstance() {
        return INSTANCE;
    }

    public static DataSource createDataSource(final String url) {
        final var jdbcDataSource = new JdbcDataSource();
        jdbcDataSource.setUrl(Objects.requireNonNull(url));
        jdbcDataSource.setUser("");
        jdbcDataSource.setPassword("");
        return jdbcDataSource;
    }

    private DataSourceConfig() {}
}
