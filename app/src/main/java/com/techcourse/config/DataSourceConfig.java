package com.techcourse.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.Objects;

public class DataSourceConfig {

    private static javax.sql.DataSource INSTANCE;

    public static javax.sql.DataSource getInstance() {
        if (Objects.isNull(INSTANCE)) {
            INSTANCE = createHikariDataSource();
        }
        return INSTANCE;
    }

    private static HikariDataSource createHikariDataSource() {
        final var hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;");
        hikariConfig.setUsername("");
        hikariConfig.setPassword("");

        return new HikariDataSource(hikariConfig);
    }

    private DataSourceConfig() {}
}
