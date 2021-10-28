package com.techcourse.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;

import java.util.Objects;

public class DataSourceConfig {

    private static javax.sql.DataSource INSTANCE;

    public static javax.sql.DataSource getInstance() {
        if (Objects.isNull(INSTANCE)) {
            INSTANCE = createDateSource();
        }
        return INSTANCE;
    }

    private static DataSource createDateSource() {
        final HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;");
        hikariConfig.setUsername("");
        hikariConfig.setPassword("");
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty( "prepStmtCacheSize" , "250" );
        hikariConfig.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );

        return new HikariDataSource(hikariConfig);
    }

    private DataSourceConfig() {}
}
