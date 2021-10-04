package com.techcourse.config;

import javax.sql.DataSource;
import nextstep.web.annotation.Configuration;
import nextstep.web.annotation.Initialize;
import org.h2.jdbcx.JdbcDataSource;

import java.util.Objects;

@Configuration
public class DataSourceConfig {

    private static DataSource INSTANCE;

    private DataSourceConfig() {}

    @Initialize
    public static DataSource getInstance() {
        if (Objects.isNull(INSTANCE)) {
            INSTANCE = createJdbcDataSource();
        }
        return INSTANCE;
    }

    private static JdbcDataSource createJdbcDataSource() {
        final JdbcDataSource jdbcDataSource = new JdbcDataSource();
        jdbcDataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;");
        jdbcDataSource.setUser("");
        jdbcDataSource.setPassword("");
        return jdbcDataSource;
    }
}
