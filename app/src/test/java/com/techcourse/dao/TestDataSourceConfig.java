package com.techcourse.dao;

import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;

public class TestDataSourceConfig {
    public static DataSource create() {
        final var jdbcDataSource = new JdbcDataSource();
        jdbcDataSource.setUrl("jdbc:h2:mem:testdb-" + System.nanoTime() + ";DB_CLOSE_DELAY=-1;");
        jdbcDataSource.setUser("");
        jdbcDataSource.setPassword("");
        return jdbcDataSource;
    }
}
