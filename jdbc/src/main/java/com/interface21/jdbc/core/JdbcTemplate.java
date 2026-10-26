package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, Object... params) {
        return execute(sql, PreparedStatement::executeUpdate, params);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... params) {
        return execute(sql, statement -> mapRows(statement, rowMapper), params);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        return execute(sql, statement -> mapSingleRow(statement, rowMapper), params);
    }

    private <T> List<T> mapRows(
            PreparedStatement statement,
            RowMapper<T> rowMapper) throws SQLException {

        try (ResultSet rs = statement.executeQuery()) {
            List<T> results = new ArrayList<>();

            while (rs.next()) {
                results.add(rowMapper.mapRow(rs));
            }

            return results;
        }
    }

    private <T> T mapSingleRow(
            PreparedStatement statement,
            RowMapper<T> rowMapper) throws SQLException {

        try (ResultSet rs = statement.executeQuery()) {
            if (!rs.next()) {
                return null;
            }

            T result = rowMapper.mapRow(rs);

            if (rs.next()) {
                throw new DataAccessException("조회 결과가 여러 건입니다.");
            }

            return result;
        }
    }

    private <T> T execute(
            String sql,
            PreparedStatementCallback<T> callback,
            Object... params) {

        Connection connection = DataSourceUtils.getConnection(dataSource);

        try {
            return execute(connection, sql, callback, params);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private <T> T execute(
            Connection connection,
            String sql,
            PreparedStatementCallback<T> callback,
            Object... params) {

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            setParameters(statement, params);
            return callback.execute(statement);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void setParameters(
            PreparedStatement statement,
            Object... params) throws SQLException {

        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
    }

    @FunctionalInterface
    private interface PreparedStatementCallback<T> {
        T execute(PreparedStatement statement) throws SQLException;
    }
}
