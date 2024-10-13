package com.techcourse.config;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;
import org.h2.jdbcx.JdbcDataSource;

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    private static javax.sql.DataSource INSTANCE;

    public static javax.sql.DataSource getInstance() {
        if (Objects.isNull(INSTANCE)) {
            INSTANCE = createJdbcDataSource();
        }
        return INSTANCE;
    }

    private static JdbcDataSource createJdbcDataSource() {
        final var jdbcDataSource = new JdbcDataSource();
        jdbcDataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;");
        jdbcDataSource.setUser("");
        jdbcDataSource.setPassword("");
        return jdbcDataSource;
    }

    private DataSourceConfig() {}

    public static Connection getConnection() {
        try {
            validateInstanceIsNull();
            return INSTANCE.getConnection();
        } catch (SQLException e) {
            log.info("GET_CONNECTION_EXCEPTION :: {}", e.getMessage(), e);
            throw new DataAccessException("DB 커넥션을 얻던 중 예외가 발생했습니다.");
        }
    }

    private static void validateInstanceIsNull() {
        if (Objects.isNull(INSTANCE)) {
            throw new IllegalStateException("유효한 DataSource가 존재하지 않습니다.");
        }
    }
}
