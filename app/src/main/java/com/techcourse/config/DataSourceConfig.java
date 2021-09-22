package com.techcourse.config;

import nextstep.datasource.DataSourceType;
import nextstep.jdbc.JdbcDataSourceBuilder;

import javax.sql.DataSource;
import java.util.Objects;

public class DataSourceConfig {

    private static DataSource INSTANCE;

    private DataSourceConfig() {
    }

    public static javax.sql.DataSource getInstance() {
        if (Objects.isNull(INSTANCE)) {
            INSTANCE = createJdbcDataSource();
        }
        return INSTANCE;
    }

    private static DataSource createJdbcDataSource() {
        String url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;";
        String user = "";
        String password = "";

        return JdbcDataSourceBuilder.create(DataSourceType.H2)
                .url(url)
                .user(user)
                .password(password)
                .build();
    }
}
