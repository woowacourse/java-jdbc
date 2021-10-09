package com.techcourse.config;

import di.annotation.Component;
import di.annotation.Configuration;
import nextstep.datasource.DataSourceType;
import nextstep.datasource.DatabasePopulator;
import nextstep.jdbc.JdbcDataSourceBuilder;
import nextstep.jdbc.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Component
    public DataSource dataSource() {
        String url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;";
        String user = "";
        String password = "";

        return JdbcDataSourceBuilder.create(DataSourceType.H2)
                .url(url)
                .user(user)
                .password(password)
                .build();
    }

    @Component
    public DatabasePopulator databasePopulator(DataSource dataSource) {
        return new DatabasePopulator(dataSource);
    }

    @Component
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
