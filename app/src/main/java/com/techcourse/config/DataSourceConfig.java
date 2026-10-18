package com.techcourse.config;

import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;
import java.util.Objects;

public class DataSourceConfig {

    private static DataSource INSTANCE;

    public static DataSource getInstance() {
        if (Objects.isNull(INSTANCE)) {
            INSTANCE = createJdbcDataSource();
        }
        return INSTANCE;
    }

    private static JdbcDataSource createJdbcDataSource() {
        final var dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:file:./java-jdbc");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        return dataSource;
    }

    private DataSourceConfig() {}
}
