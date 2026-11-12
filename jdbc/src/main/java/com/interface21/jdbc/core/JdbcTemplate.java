package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
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
        execute(sql, PreparedStatement::executeUpdate, params);
    }

    public void update(Connection connection, String sql, Object... params) {
        execute(connection, sql, PreparedStatement::executeUpdate, params);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... params) {
        return execute(sql, pstmt -> mapRows(pstmt, rowMapper), params);
    }

    public <T> List<T> query(Connection connection, String sql, RowMapper<T> rowMapper, Object... params) {
        return execute(connection, sql, pstmt -> mapRows(pstmt, rowMapper), params);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        return singleResult(query(sql, rowMapper, params));
    }

    public <T> T queryForObject(Connection connection, String sql, RowMapper<T> rowMapper, Object... params) {
        return singleResult(query(connection, sql, rowMapper, params));
    }

    private <T> T singleResult(List<T> results) {
        if (results.size() != 1) {
            throw new IllegalStateException("Expected 1 row but found " + results.size());
        }
        return results.getFirst();
    }

    private <T> T execute(String sql, PreparedStatementCallback<T> action, Object... params) {
        try (Connection connection = dataSource.getConnection()) {
            return executeStatement(connection, sql, action, params);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> T execute(Connection connection, String sql, PreparedStatementCallback<T> action, Object... params) {
        try {
            return executeStatement(connection, sql, action, params);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> T executeStatement(Connection connection, String sql, PreparedStatementCallback<T> action,
                                   Object... params) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            setParameters(pstmt, params);
            log.debug("query : {}", sql);
            return action.doInPreparedStatement(pstmt);
        }
    }

    private <T> List<T> mapRows(PreparedStatement statement, RowMapper<T> rowMapper) throws SQLException {
        try (ResultSet rs = statement.executeQuery()) {
            List<T> results = new ArrayList<>();
            while (rs.next()) {
                results.add(rowMapper.mapRow(rs));
            }
            return results;
        }
    }

    private void setParameters(PreparedStatement pstmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
    }
}
