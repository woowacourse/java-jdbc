package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.interface21.jdbc.JdbcException;
import com.interface21.jdbc.core.mapper.RowMapper;
import com.interface21.jdbc.core.sql.Sql;

public class JdbcTemplate {

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(final String sql, final SqlParameterSource parameters) {
        try (final Connection conn = dataSource.getConnection();
             final Statement stmt = conn.createStatement()) {
            final Sql bindingParametersQuery = new Sql(sql, parameters);
            execWriteQuery(stmt, bindingParametersQuery);
        } catch (final SQLException e) {
            throw new JdbcException(e.getMessage(), e);
        }
    }

    public void insert(final Connection connection, final String sql, final SqlParameterSource parameters) {
        try (final Statement stmt = connection.createStatement()) {
            final Sql bindingParametersQuery = new Sql(sql, parameters);
            execWriteQuery(stmt, bindingParametersQuery);
        } catch (final SQLException e) {
            throw new JdbcException(e.getMessage(), e);
        }
    }

    private void execWriteQuery(final Statement statement, final Sql sql) {
        try {
            final String query = sql.getValue();
            statement.executeUpdate(query);
        } catch (final SQLException e) {
            throw new JdbcException(e.getMessage(), e);
        }
    }

    public void update(final String sql, final Map<String, Object> parameters) {
        try (final Connection conn = dataSource.getConnection();
             final Statement stmt = conn.createStatement()) {
            final Sql bindingParametersQuery = new Sql(sql, parameters);
            execWriteQuery(stmt, bindingParametersQuery);
        } catch (final SQLException e) {
            throw new JdbcException(e.getMessage(), e);
        }
    }

    public void update(final Connection connection, final String sql, final Map<String, Object> parameters) {
        try (final Statement stmt = connection.createStatement()) {
            final Sql bindingParametersQuery = new Sql(sql, parameters);
            execWriteQuery(stmt, bindingParametersQuery);
        } catch (final SQLException e) {
            throw new JdbcException(e.getMessage(), e);
        }
    }

    public <T> T queryForObject(
            final String sql,
            final Map<String, Object> parameters,
            final RowMapper<T> rowMapper
    ) {
        try (final Connection conn = dataSource.getConnection();
             final Statement stmt = conn.createStatement()) {
            final Sql bindingParametersQuery = new Sql(sql, parameters);
            final ResultSet resultSet = execReadQuery(stmt, bindingParametersQuery);

            if (resultSet.next()) {
                return rowMapper.mapping(resultSet);
            }

            return null;
        } catch (final SQLException e) {
            throw new JdbcException(e.getMessage(), e);
        }
    }

    private ResultSet execReadQuery(final Statement stmt, final Sql sql) {
        try {
            final String query = sql.getValue();
            return stmt.executeQuery(query);
        } catch (final SQLException e) {
            throw new JdbcException(e.getMessage(), e);
        }
    }

    public <T> List<T> query(
            final String sql,
            final Map<String, Object> parameters,
            final RowMapper<T> rowMapper
    ) {
        try (final Connection conn = dataSource.getConnection();
             final Statement stmt = conn.createStatement()) {
            final Sql bindingParametersQuery = new Sql(sql, parameters);
            final ResultSet resultSet = execReadQuery(stmt, bindingParametersQuery);

            final List<T> result = new ArrayList<>();
            while (resultSet.next()) {
                result.add(rowMapper.mapping(resultSet));
            }

            return result;
        } catch (final SQLException e) {
            throw new JdbcException(e.getMessage(), e);
        }
    }
}
