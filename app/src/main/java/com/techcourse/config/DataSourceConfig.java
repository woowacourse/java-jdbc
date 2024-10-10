package com.techcourse.config;

import com.interface21.context.stereotype.Bean;
import com.interface21.context.stereotype.Configuration;
import org.h2.jdbcx.JdbcDataSource;

import java.util.Objects;

@Configuration
public class DataSourceConfig {

    private static javax.sql.DataSource INSTANCE;

    @Bean
    public static javax.sql.DataSource getInstance() {
        if (Objects.isNull(INSTANCE)) {
            INSTANCE = createJdbcDataSource();
        }
        return INSTANCE;
    }

    private static JdbcDataSource createJdbcDataSource() {
        JdbcDataSource jdbcDataSource = new JdbcDataSource();
        jdbcDataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;");
        jdbcDataSource.setUser("");
        jdbcDataSource.setPassword("");
        return jdbcDataSource;
    }

    private DataSourceConfig() {}
}
