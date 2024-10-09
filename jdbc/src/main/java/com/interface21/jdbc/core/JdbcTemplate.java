package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.interface21.jdbc.core.exception.JdbcSQLException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    public static final int START_ARGUMENT_COUNT = 1;

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, Object... arguments) {
        log.debug("update query : {}", sql);

        return execute(sql, PreparedStatement::executeUpdate, arguments);
    }

    public <T> T queryObject(String sql, RowMapper<T> rowMapper, Object... arguments) {
        List<T> result = query(sql, rowMapper, arguments);
        if (result.isEmpty()) {
            return null;
        }
        return result.getFirst();
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... arguments) {
        log.debug("query : {}", sql);

        return execute(sql, preparedStatement -> fetchResults(rowMapper, preparedStatement), arguments);
    }

    private <T> List<T> fetchResults(RowMapper<T> rowMapper, PreparedStatement preparedStatement)
            throws SQLException {
        ResultSet resultSet = preparedStatement.executeQuery();
        List<T> objects = new ArrayList<>();
        while (resultSet.next()) {
            objects.add(rowMapper.mapRow(resultSet, resultSet.getFetchSize()));
        }
        return objects;
    }

    public <T> T execute(String sql, PreparedStatementExecutor<T> preparedStatementExecutor, Object... arguments) {
        log.debug("update query : {}", sql);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setArguments(arguments, preparedStatement);

            return preparedStatementExecutor.excute(preparedStatement);
        } catch (SQLException e) {
            String errorMessage = String.format("Error executing: %s with arguments: %s", sql,
                    Arrays.toString(arguments));
            log.error(errorMessage);
            throw new JdbcSQLException(errorMessage, e);
        }
    }

    private void setArguments(Object[] arguments, PreparedStatement preparedStatement) throws SQLException {
        int count = START_ARGUMENT_COUNT;
        for (Object argument : arguments) {
            preparedStatement.setObject(count, argument);
            count++;
        }
    }
}
