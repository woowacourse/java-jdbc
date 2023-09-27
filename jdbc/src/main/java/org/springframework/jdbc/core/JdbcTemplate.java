package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
            throw new IllegalArgumentException("Update Exception");
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
        } catch (Exception e) {
            throw new IllegalArgumentException("Find Exception");
        } finally {
            closeResources(connection, preparedStatement, resultSet);
        }
    }

    public <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = getConnection();
            preparedStatement = getPreparedStatement(connection, sql, args);
            resultSet = getResultSet(preparedStatement);
            return getObject(resultSet, rowMapper);
        } catch (Exception e) {
            throw new IllegalArgumentException("Find Exception");
        } finally {
            closeResources(connection, preparedStatement, resultSet);
        }
    }

    private Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new IllegalArgumentException("Connection cannot be acquired.");
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
            throw new IllegalArgumentException("PreparedStatement cannot be acquired.");
        }
    }

    private ResultSet getResultSet(PreparedStatement preparedStatement) {
        try {
            return preparedStatement.executeQuery();
        } catch (SQLException e) {
            throw new IllegalArgumentException("ResultSet cannot be acquired.");
        }
    }

    private <T> List<T> getObjects(ResultSet resultSet, RowMapper<T> rowMapper) {
        List<T> results = new ArrayList<>();
        while (true) {
            Optional<T> result = getObject(resultSet, rowMapper);
            if (result.isPresent()) {
                results.add(result.get());
                continue;
            }
            break;
        }
        return results;
    }

    private <T> Optional<T> getObject(ResultSet resultSet, RowMapper<T> rowMapper) {
        try {
            if (resultSet.next()) {
                return Optional.of(rowMapper.map(resultSet));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new IllegalArgumentException("Mapping fail");
        }
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
