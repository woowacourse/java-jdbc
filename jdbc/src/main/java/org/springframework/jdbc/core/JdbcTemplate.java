package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.exception.DatabaseResourceException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object... args) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = getConnection();
            preparedStatement = getPreparedStatement(connection, sql, args);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseResourceException("Timeout or PreparedStatement is already closed", e);
        } finally {
            closeResources(connection, preparedStatement, resultSet);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = getConnection();
            preparedStatement = getPreparedStatement(connection, sql, args);
            resultSet = getResultSet(preparedStatement);
            return getObjects(resultSet, rowMapper);
        } finally {
            closeResources(connection, preparedStatement, resultSet);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        List<T> results = query(sql, rowMapper, args);
        if (results.isEmpty()) {
            throw new DataAccessException("Data not found.");
        }
        if (results.size() >= 2) {
            throw new DataAccessException("More than one data found");
        }
        return results.get(0);
    }

    private Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new DatabaseResourceException("Connection cannot be acquired.", e);
        }
    }

    private PreparedStatement getPreparedStatement(Connection connection, String sql, Object... args) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            for (int idx = 1; idx <= args.length; idx++) {
                preparedStatement.setObject(idx, args[idx - 1]);
            }
            return preparedStatement;
        } catch (SQLException e) {
            throw new DatabaseResourceException("PreparedStatement cannot be acquired.", e);
        }
    }

    private ResultSet getResultSet(PreparedStatement preparedStatement) {
        try {
            return preparedStatement.executeQuery();
        } catch (SQLException e) {
            throw new DatabaseResourceException(
                    "ResultSet cannot be acquired or PreparedStatement is already closed.", e);
        }
    }

    private <T> List<T> getObjects(ResultSet resultSet, RowMapper<T> rowMapper) {
        List<T> results = new ArrayList<>();
        try {
            while (resultSet.next()) {
                T result = rowMapper.map(resultSet);
                results.add(result);
            }
        } catch (SQLException e) {
            throw new DatabaseResourceException("ResultSet is already closed", e);
        }
        return results;
    }

    private void closeResources(Connection connection,
                                PreparedStatement preparedStatement,
                                ResultSet resultSet) {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
        } catch (SQLException ignored) {
        }

        try {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
        } catch (SQLException ignored) {
        }

        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException ignored) {
        }
    }
}
