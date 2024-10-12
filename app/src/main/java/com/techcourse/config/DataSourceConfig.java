package com.techcourse.config;

import com.techcourse.exception.TechCourseApplicationException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import org.h2.jdbcx.JdbcDataSource;

public class DataSourceConfig {

    private static javax.sql.DataSource INSTANCE;

    public static javax.sql.DataSource getInstance() {
        if (Objects.isNull(INSTANCE)) {
            INSTANCE = createJdbcDataSource();
        }
        return INSTANCE;
    }

    public static Connection getConnection() {
        try {
            return getInstance().getConnection();
        } catch (SQLException e) {
            throw new TechCourseApplicationException("DB 커넥션을 가져오는 것에 실패했습니다", e);
        }
    }

    private static JdbcDataSource createJdbcDataSource() {
        final var jdbcDataSource = new JdbcDataSource();
        jdbcDataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;");
        jdbcDataSource.setUser("");
        jdbcDataSource.setPassword("");
        return jdbcDataSource;
    }

    private DataSourceConfig() {
    }
}
