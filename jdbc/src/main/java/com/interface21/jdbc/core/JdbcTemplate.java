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

public class JdbcTemplate {

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, PreparedStatementSetter setter) {
        return execute(sql, preparedStatement -> {
            setter.setValues(preparedStatement);
            return preparedStatement.executeUpdate();
        });
    }

    public int update(String sql, Object... args) {
        return update(sql, setterOf(args));
    }

    public int update(Connection connection, String sql, PreparedStatementSetter setter) {
        return execute(connection, sql, preparedStatement -> {
            setter.setValues(preparedStatement);
            return preparedStatement.executeUpdate();
        });
    }

    public int update(Connection connection, String sql, Object... args) {
        return update(connection, sql, setterOf(args));
    }

    private PreparedStatementSetter setterOf(Object... args) {
        return statement -> {
            for (int i = 0; i < args.length; i++) {
                statement.setObject(i + 1, args[i]);
            }
        };
    }

    public <T> List<T> select(
            String sql,
            PreparedStatementSetter setter,
            RowMapper<T> mapper
    ) {
        return execute(sql, selectCallback(setter, mapper));
    }

    private <T> StatementCallback<List<T>> selectCallback(PreparedStatementSetter setter, RowMapper<T> mapper) {
        return preparedStatement -> {
            setter.setValues(preparedStatement);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                List<T> results = new ArrayList<>();
                while (resultSet.next()) {
                    results.add(mapper.mapRow(resultSet));
                }
                return results;
            }
        };
    }

    public <T> List<T> select(
            String sql,
            RowMapper<T> mapper,
            Object... args
    ) {
        return select(sql, setterOf(args), mapper);
    }

    public <T> T selectOne(
            String sql,
            RowMapper<T> mapper,
            Object... args
    ) {
        return singleResult(select(sql, mapper, args));
    }

    private <T> T singleResult(List<T> results) {
        if (results.size() != 1) {
            throw new DataAccessException("Expected exactly one row, but found " + results.size());
        }
        return results.getFirst();
    }

    private <T> T execute(String sql, StatementCallback<T> callback) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            return execute(connection, sql, callback);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private <T> T execute(Connection connection, String sql, StatementCallback<T> callback) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            return callback.doInStatement(preparedStatement);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    @FunctionalInterface
    private interface StatementCallback<T> {
        T doInStatement(PreparedStatement preparedStatement) throws SQLException;
    }
}
