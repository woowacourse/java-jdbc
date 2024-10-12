package com.techcourse.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;

import com.interface21.dao.DataAccessException;

public class DataSourceConfig {

    private static final Connection txConnection;
    private static DataSource INSTANCE;

    static {
        INSTANCE = createJdbcDataSource();
        try {
            txConnection = INSTANCE.getConnection();
        } catch (SQLException e) {
            throw new DataAccessException("고정 커넥션 생성 중 예외 발생");
        }
    }

    private DataSourceConfig() {
    }

    public static DataSource getInstance() {
        if (Objects.isNull(INSTANCE)) {
            INSTANCE = createJdbcDataSource();
        }
        return INSTANCE;
    }

    public static Connection getTxConnection() {
        return txConnection;
    }

    private static JdbcDataSource createJdbcDataSource() {
        final var jdbcDataSource = new JdbcDataSource();
        jdbcDataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;");
        jdbcDataSource.setUser("");
        jdbcDataSource.setPassword("");
        return jdbcDataSource;
    }
}
