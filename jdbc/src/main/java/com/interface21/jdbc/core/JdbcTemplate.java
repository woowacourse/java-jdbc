package com.interface21.jdbc.core;

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
    private static final int SINGLE_RESULT_SIZE = 1;

    private final DataSource dataSource;
    private final PreparedStatementSetter statementSetter;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
        this.statementSetter = new PreparedStatementSetter();
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... arguments) {
        try (Connection connection = dataSource.getConnection()) {
            return queryWithConnection(connection, sql, rowMapper, arguments);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new JdbcException("An error occurred during the execution of the select query.", e);
        }
    }

    public <T> List<T> queryWithConnection(final Connection connection, final String sql,
                                           final RowMapper<T> rowMapper, final Object... arguments) {
        validateConnection(connection);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            statementSetter.setValues(preparedStatement, arguments);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                List<T> results = new ArrayList<>();
                while (resultSet.next()) {
                    results.add(rowMapper.mapRow(resultSet));
                }
                return results;
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new JdbcException("An error occurred during the execution of the select query.", e);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... arguments) {
        List<T> results = query(sql, rowMapper, arguments);

        if (results.size() > SINGLE_RESULT_SIZE) {
            throw new JdbcException("multiple rows found.");
        }
        if (results.size() == SINGLE_RESULT_SIZE) {
            return results.getFirst();
        }
        return null;
    }

    public <T> T queryForObjectWithConnection(final Connection connection, final String sql,
                                              final RowMapper<T> rowMapper, final Object... arguments) {
        List<T> results = queryWithConnection(connection, sql, rowMapper, arguments);

        if (results.size() > SINGLE_RESULT_SIZE) {
            throw new JdbcException("multiple rows found.");
        }
        if (results.size() == SINGLE_RESULT_SIZE) {
            return results.getFirst();
        }
        return null;
    }

    public void update(final String sql, final Object... arguments) {
        try (Connection connection = dataSource.getConnection()) {
            updateWithConnection(connection, sql, arguments);
        } catch (SQLException e) {
            throw new JdbcException("An error occurred during the execution of the update query.", e);
        }
    }

    public void updateWithConnection(final Connection connection, final String sql, final Object... arguments) {
        validateConnection(connection);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            statementSetter.setValues(preparedStatement, arguments);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new JdbcException("An error occurred during the execution of the update query.", e);
        }
    }

    private void validateConnection(final Connection connection) {
        if (connection == null) {
            throw new JdbcException("connection cannot be null.");
        }
    }
}
