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
        return execute(sql, statement -> {
            try (ResultSet resultSet = executeQuery(statementSetter, statement)) {
                return mapRows(resultSet, rowMapper);
            }
        });
    }

    public final void update(String sql, Object... args) {
        update(sql, parameterSetter(args));
    }

    public final void update(String sql, PreparedStatementSetter statementSetter) {
        execute(sql, statement -> {
            statementSetter.setValues(statement);
            return statement.executeUpdate();
        });
    }

    protected ResultSet executeQuery(PreparedStatementSetter statementSetter, PreparedStatement statement)
            throws SQLException {
        statementSetter.setValues(statement);
        return statement.executeQuery();
    }

    private <T> T execute(String sql, PreparedStatementCallback<T> callback) {
        try (Connection connection = getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            return callback.execute(statement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
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
