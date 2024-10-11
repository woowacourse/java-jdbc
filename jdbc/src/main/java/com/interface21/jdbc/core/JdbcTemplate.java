package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import javax.sql.DataSource;

import org.apache.commons.lang3.function.TriFunction;

import com.interface21.jdbc.core.mapper.RowMapper;
import com.interface21.jdbc.core.sql.Sql;

public class JdbcTemplate {

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(final String sql, final SqlParameterSource parameters) {
        final Sql bindingParametersQuery = new Sql(sql, parameters);
        final BiConsumer<Statement, Sql> insertCallBack = this::execWriteQuery;
        execute(insertCallBack, bindingParametersQuery);
    }

    private void execWriteQuery(final Statement statement, final Sql sql) {
        try {
            final String query = sql.getValue();
            statement.executeUpdate(query);
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void update(final String sql, final Map<String, Object> parameters) {
        final Sql bindingParametersQuery = new Sql(sql, parameters);
        final BiConsumer<Statement, Sql> updateCallBack = this::execWriteQuery;
        execute(updateCallBack, bindingParametersQuery);
    }

    public <T> T queryForObject(
            final String sql,
            final Map<String, Object> parameters,
            final RowMapper<T> rowMapper
    ) {
        final Sql bindingParametersQuery = new Sql(sql, parameters);
        final TriFunction<Statement, Sql, RowMapper<T>, List<T>> queryForObjectCallBack = (stmt, query, mapper) -> {
            final ResultSet resultSet = execReadQuery(stmt, query);
            return List.of(mapper.mapping(resultSet));
        };
        final List<T> result = execute(queryForObjectCallBack, bindingParametersQuery, rowMapper);
        return result.getFirst();
    }

    private ResultSet execReadQuery(final Statement stmt, final Sql sql) {
        try {
            final String query = sql.getValue();
            return stmt.executeQuery(query);
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> query(
            final String sql,
            final Map<String, Object> parameters,
            final RowMapper<T> rowMapper
    ) {
        final Sql bindingParametersQuery = new Sql(sql, parameters);
        final TriFunction<Statement, Sql, RowMapper<T>, List<T>> queryCallBack = (stmt, query, mapper) -> {
            final ResultSet resultSet = execReadQuery(stmt, query);
            return parseResult(resultSet, rowMapper);
        };

        return execute(queryCallBack, bindingParametersQuery, rowMapper);
    }

    private <T> List<T> parseResult(final ResultSet resultSet, final RowMapper<T> rowMapper) {
        final List<T> result = new ArrayList<>();
        T mappingResult = rowMapper.mapping(resultSet);
        while (mappingResult != null) {
            result.add(mappingResult);
            mappingResult = rowMapper.mapping(resultSet);
        }
        return result;
    }

    private void execute(final BiConsumer<Statement, Sql> callBack, final Sql sql) {
        try (final Connection conn = dataSource.getConnection(); final Statement stmt = conn.createStatement()) {
            callBack.accept(stmt, sql);
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> List<T> execute(
            final TriFunction<Statement, Sql, RowMapper<T>, List<T>> callBack,
            final Sql sql,
            final RowMapper<T> rowMapper
    ) {
        try (final Connection conn = dataSource.getConnection(); final Statement stmt = conn.createStatement()) {
            return callBack.apply(stmt, sql, rowMapper);
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
