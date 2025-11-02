package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object... params) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        execute(connection, sql, pstmt -> {
            pstmt.executeUpdate();
            return null;
        }, params);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... params) {
        return execute(sql, preparedStatement -> {
            try (ResultSet rs = preparedStatement.executeQuery()) {
                log.debug("Executed SQL: {}", sql);
                List<T> results = new ArrayList<>();
                int rowNum = 0;
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs, rowNum++));
                }
                return results;
            }
        }, params);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        return execute(sql, preparedStatement -> {
            try (ResultSet rs = preparedStatement.executeQuery()) {
                log.debug("Executed SQL: {}", sql);
                int rowNum = 0;
                if (rs.next()) {
                    T result = rowMapper.mapRow(rs, rowNum++);
                    if (rs.next()) {
                        throw new IllegalStateException(
                                "Expected one result (1 row), but query returned more than one row.");
                    }
                    return result;
                }
                throw new IllegalStateException("Expected one result (1 row), but query returned none.");
            }
        }, params);
    }

    public <T> T queryForObject(Connection connection, String sql, RowMapper<T> rowMapper, Object... params) {
        return execute(connection, sql, pstmt -> {
            try (ResultSet rs = pstmt.executeQuery()) {
                int rowNum = 0;
                if (rs.next()) {
                    T result = rowMapper.mapRow(rs, rowNum++);
                    log.debug("Executed SQL: {}", sql);
                    return result;
                }
                log.debug("Executed SQL: {}", sql);
                return null;
            }
        }, params);
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    private <T> T execute(String sql, PreparedStatementCallback<T> action, Object... params) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            bindParameters(preparedStatement, params);
            T result = action.doInPreparedStatement(preparedStatement);
            return result;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> T execute(Connection connection, String sql, PreparedStatementCallback<T> action, Object... params) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            bindParameters(preparedStatement, params);
            T result = action.doInPreparedStatement(preparedStatement);
            log.debug("Executed SQL: {}", sql);
            return result;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private void bindParameters(PreparedStatement pstmt, Object... params) throws SQLException {
        validateParameterCount(pstmt, params);
        for (int i = 1; i <= params.length; i++) {
            pstmt.setObject(i, params[i - 1]);
        }
    }

    private void validateParameterCount(PreparedStatement pstmt, Object[] params) {
        try {
            int expected = pstmt.getParameterMetaData().getParameterCount();
            int actual = (params == null) ? 0 : params.length;
            if (expected != actual) {
                throw new IllegalArgumentException(
                        String.format("SQL parameter count mismatch: expected %d but got %d", expected, actual));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to read PreparedStatement parameter metadata", e);
        }
    }
}
