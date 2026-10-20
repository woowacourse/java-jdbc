package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;

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
        return execute(sql, statement -> {
            try (ResultSet rs = statement.executeQuery()) {
                List<T> results = new ArrayList<>();

                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs));
                }

                return results;
            }
        }, params);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        List<T> results = query(sql, rowMapper, params);

        if (results.isEmpty()) {
            return null;
        }

        if (results.size() > 1) {
            throw new DataAccessException(
                    "조회 결과가 여러 건입니다. resultSize=" + results.size()
            );
        }

        return results.get(0);
    }

    private <T> T execute(
            String sql,
            PreparedStatementCallback<T> callback,
            Object... params) {

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

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
