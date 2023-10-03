package com.techcourse.config;

import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;

public class DataSourceConfig {

    private DataSourceConfig() {
    }

    public static DataSource getInstance() {
        return Holder.INSTANCE;
    }

    private static class Holder {

        private static final DataSource INSTANCE = createJdbcDataSource();

        private static DataSource createJdbcDataSource() {
            final var jdbcDataSource = new JdbcDataSource();
            jdbcDataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;");
            jdbcDataSource.setUser("");
            jdbcDataSource.setPassword("");
            return jdbcDataSource;
        }
    }
}
