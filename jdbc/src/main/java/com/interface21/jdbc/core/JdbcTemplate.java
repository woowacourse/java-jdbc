package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
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

    public void update(String sql, PreparedStatementSetter preparedStatementSetter) {
        execute(sql, preparedStatementSetter, (preparedStatement) -> {
            preparedStatement.executeUpdate();
            return null;
        });
    }

    public void update(String sql, Connection connection, PreparedStatementSetter preparedStatementSetter) {
        executeWithExternalConnection(sql, connection, preparedStatementSetter, (preparedStatement) -> {
            preparedStatement.executeUpdate();
            return null;
        });
    }

    private <T> T execute(String sql, PreparedStatementSetter preparedStatementSetter,
                          PreparedStatementStrategy<T> strategy) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatementSetter.setValues(preparedStatement);
            return strategy.execute(preparedStatement);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(dataSource);
        }
    }
    
    private <T> T executeWithExternalConnection(String sql, Connection connection,
                                                PreparedStatementSetter preparedStatementSetter,
                                                PreparedStatementStrategy<T> strategy) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatementSetter.setValues(preparedStatement);
            return strategy.execute(preparedStatement);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> query(String sql, PreparedStatementSetter preparedStatementSetter, RowMapper<T> rowMapper) {
        return execute(sql, preparedStatementSetter, preparedStatement -> getResults(rowMapper, preparedStatement));
    }

    private <T> List<T> getResults(RowMapper<T> rowMapper, PreparedStatement preparedStatement) throws SQLException {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            return readResults(rowMapper, resultSet);
        }
    }

    private <T> List<T> readResults(RowMapper<T> rowMapper, ResultSet resultSet) throws SQLException {
        List<T> results = new ArrayList<>();
        while (resultSet.next()) {
            results.add(rowMapper.map(resultSet));
        }
        return Collections.unmodifiableList(results);
    }

    public <T> T queryForObject(String sql, PreparedStatementSetter preparedStatementSetter, RowMapper<T> rowMapper) {
        return execute(sql, preparedStatementSetter,
                preparedStatement -> getSingleObject(getResults(rowMapper, preparedStatement)));
    }

    private <T> T getSingleObject(List<T> results) {
        if (results.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(1, results.size());
        }
        return results.getFirst();
    }
}
