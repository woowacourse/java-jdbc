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

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public final DataSource getDataSource() {
        return dataSource;
    }

    public final <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        return query(sql, parameterSetter(args), rowMapper);
    }

    public final <T> List<T> query(String sql, PreparedStatementSetter statementSetter, RowMapper<T> rowMapper) {
        return execute(sql, queryCallback(statementSetter, rowMapper));
    }

    public final <T> List<T> query(Connection connection, String sql, RowMapper<T> rowMapper, Object... args) {
        return query(connection, sql, parameterSetter(args), rowMapper);
    }

    public final <T> List<T> query(Connection connection, String sql, PreparedStatementSetter statementSetter,
                                 RowMapper<T> rowMapper) {
        return execute(connection, sql, queryCallback(statementSetter, rowMapper));
    }

    public final void update(String sql, Object... args) {
        update(sql, parameterSetter(args));
    }

    public final void update(String sql, PreparedStatementSetter statementSetter) {
        execute(sql, updateCallback(statementSetter));
    }

    public final void update(Connection connection, String sql, Object... args) {
        update(connection, sql, parameterSetter(args));
    }

    public final void update(Connection connection, String sql, PreparedStatementSetter statementSetter) {
        execute(connection, sql, updateCallback(statementSetter));
    }

    protected ResultSet executeQuery(PreparedStatementSetter statementSetter, PreparedStatement statement)
            throws SQLException {
        statementSetter.setValues(statement);
        return statement.executeQuery();
    }

    private <T> T execute(String sql, PreparedStatementCallback<T> callback) {
        try (Connection connection = getDataSource().getConnection()) {
            return executeStatement(connection, sql, callback);
        } catch (SQLException e) {
            throw translateException(e);
        }
    }

    private <T> T execute(Connection connection, String sql, PreparedStatementCallback<T> callback) {
        try {
            return executeStatement(connection, sql, callback);
        } catch (SQLException e) {
            throw translateException(e);
        }
    }

    private <T> T executeStatement(Connection connection, String sql, PreparedStatementCallback<T> callback)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            return callback.execute(statement);
        }
    }

    private DataAccessException translateException(SQLException exception) {
        log.error(exception.getMessage(), exception);
        return new DataAccessException(exception);
    }

    private <T> PreparedStatementCallback<List<T>> queryCallback(PreparedStatementSetter statementSetter,
                                                               RowMapper<T> rowMapper) {
        return statement -> {
            try (ResultSet resultSet = executeQuery(statementSetter, statement)) {
                return mapRows(resultSet, rowMapper);
            }
        };
    }

    private PreparedStatementCallback<Integer> updateCallback(PreparedStatementSetter statementSetter) {
        return statement -> {
            statementSetter.setValues(statement);
            return statement.executeUpdate();
        };
    }

    private PreparedStatementSetter parameterSetter(Object[] args) {
        return statement -> {
            for (int parameterIndex = 0; parameterIndex < args.length; parameterIndex++) {
                statement.setObject(parameterIndex + 1, args[parameterIndex]);
            }
        };
    }

    private <T> List<T> mapRows(ResultSet resultSet, RowMapper<T> rowMapper) throws SQLException {
        List<T> rows = new ArrayList<>();
        while (resultSet.next()) {
            rows.add(rowMapper.mapRow(resultSet));
        }
        return rows;
    }

    @FunctionalInterface
    private interface PreparedStatementCallback<T> {

        T execute(PreparedStatement statement) throws SQLException;
    }
}
