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

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object... params) {
        execute(sql, PreparedStatement::executeUpdate, params);
    }

    public void update(Connection connection, String sql, Object... params) {
        execute(connection, sql, PreparedStatement::executeUpdate, params);
    }

    public <T> T find(String sql, RowMapper<T> rowMapper, Object... params) {
        return execute(sql, query(rs -> mapSingleRow(rs, rowMapper)), params);
    }

    public <T> T find(Connection connection, String sql, RowMapper<T> rowMapper, Object... params) {
        return execute(connection, sql, query(rs -> mapSingleRow(rs, rowMapper)), params);
    }

    private <T> T mapSingleRow(ResultSet rs, RowMapper<T> rowMapper) throws SQLException {
        if (!rs.next()) {
            throw new IllegalStateException("단건 조회 결과가 없습니다.");
        }

        T result = rowMapper.mapRow(rs);

        if (rs.next()) {
            throw new IllegalStateException("단건 조회 결과가 2건 이상입니다.");
        }
        return result;
    }

    public <T> List<T> findAll(String sql, RowMapper<T> rowMapper, Object... params) {
        return execute(sql, query(rs -> {
            List<T> results = new ArrayList<>();
            while (rs.next()) {
                results.add(rowMapper.mapRow(rs));
            }
            return results;
        }), params);
    }

    private <T> SqlOperation<PreparedStatement, T> query(SqlOperation<ResultSet, T> operation) {
        return pstmt -> {
            try (ResultSet rs = pstmt.executeQuery()) {
                return operation.apply(rs);
            }
        };
    }

    private <T> T execute(String sql, SqlOperation<PreparedStatement, T> operation, Object... params) {
        try (Connection connection = dataSource.getConnection()) {
            return execute(connection, sql, operation, params);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private <T> T execute(Connection connection, String sql, SqlOperation<PreparedStatement, T> operation,
                          Object... params) {
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            setParameter(params, pstmt);
            return operation.apply(pstmt);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void setParameter(Object[] params, PreparedStatement pstmt) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
    }

    @FunctionalInterface
    private interface SqlOperation<S, T> {

        T apply(S source) throws SQLException;
    }
}
