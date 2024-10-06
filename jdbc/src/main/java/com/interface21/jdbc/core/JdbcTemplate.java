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
    private static final int FIRST_PARAMETER_INDEX = 1;

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args)
            throws DataAccessException {
        return execute(sql, statement -> query(rowMapper, statement), args);
    }

    private <T> List<T> query(final RowMapper<T> rowMapper, final PreparedStatement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            return extractData(rowMapper, resultSet);
        }
    }

    private <T> List<T> extractData(final RowMapper<T> rowMapper, final ResultSet resultSet) throws SQLException {
        List<T> results = new ArrayList<>();
        while (resultSet.next()) {
            T result = rowMapper.mapRow(resultSet);
            results.add(result);
        }
        return results;
    }

    public int update(final String sql, final Object... args) throws DataAccessException {
        return execute(sql, this::update, args);
    }

    private int update(final PreparedStatement statement) throws SQLException {
        return statement.executeUpdate();
    }

    private <T> T execute(final String sql, final PreparedStatementCallback<T> action, final Object... args) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setPreparedStatementArgs(statement, args);
            return action.doInPreparedStatement(statement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private void setPreparedStatementArgs(final PreparedStatement statement, final Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            statement.setObject(FIRST_PARAMETER_INDEX + i, args[i]);
        }
    }
}
