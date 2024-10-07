package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object... params) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = buildPreparedStatement(connection, sql, params)) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private PreparedStatement buildPreparedStatement(Connection connection, String sql, Object[] params)
            throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        setParams(params, preparedStatement);
        return preparedStatement;
    }

    private void setParams(Object[] params, PreparedStatement preparedStatement) throws SQLException {
        for (int index = 0; index < params.length; index++) {
            preparedStatement.setObject(index + 1, params[index]);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... params) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = buildPreparedStatement(connection, sql, params);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            return getResults(rowMapper, resultSet);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private <T> List<T> getResults(RowMapper<T> rowMapper, ResultSet resultSet) throws SQLException {
        List<T> results = new ArrayList<>();
        while (resultSet.next()) {
            results.add(rowMapper.map(resultSet));
        }
        return Collections.unmodifiableList(results);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = buildPreparedStatement(connection, sql, params);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            return getSingleObject(getResults(rowMapper, resultSet));
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private <T> T getSingleObject(List<T> results) {
        if (results.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(1, results.size());
        }
        return results.getFirst();
    }
}
