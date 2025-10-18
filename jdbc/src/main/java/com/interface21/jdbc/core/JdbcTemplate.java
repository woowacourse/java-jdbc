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

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object... args) {
        execute(sql, PreparedStatement::executeUpdate, args);
    }

    public void update(Connection connection, String sql, Object... args) {
        execute(connection, sql, PreparedStatement::executeUpdate, args);
    }

    private <T> T execute(String sql, PreparedStatementCallback<T> callback, Object... args) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setParameters(preparedStatement, args);
            return callback.doInPreparedStatement(preparedStatement);
        } catch (SQLException e) {
            log.error("query 실패 {}", sql, e);
            throw new JdbcFailException(e);
        }
    }

    private <T> T execute(Connection connection, String sql, PreparedStatementCallback<T> callback, Object... args) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setParameters(preparedStatement, args);
            return callback.doInPreparedStatement(preparedStatement);
        } catch (SQLException e) {
            log.error("query 실패 {}", sql, e);
            throw new JdbcFailException(e);
        }
    }

    private void setParameters(PreparedStatement preparedStatement, Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            preparedStatement.setObject(i + 1, args[i]);
        }
    }

    public <T> List<T> queryForObjects(String sql, RowMapper<T> rowMapper, Object... args) {
        PreparedStatementCallback<List<T>> callback = preparedStatement -> {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                List<T> results = new ArrayList<>();
                int rowNum = 0;
                while (resultSet.next()) {
                    results.add(rowMapper.mapRow(resultSet, rowNum++));
                }
                return results;
            }
        };
        return execute(sql, callback, args);
    }

    public <T> List<T> queryForObjects(Connection connection, String sql, RowMapper<T> rowMapper, Object... args) {
        PreparedStatementCallback<List<T>> callback = preparedStatement -> {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                List<T> results = new ArrayList<>();
                int rowNum = 0;
                while (resultSet.next()) {
                    results.add(rowMapper.mapRow(resultSet, rowNum++));
                }
                return results;
            }
        };
        return execute(connection, sql, callback, args);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        List<T> results = queryForObjects(sql, rowMapper, args);
        if (results.isEmpty()) {
            throw new JdbcFailException("일치하는 결과가 없습니다. ");
        }
        if (results.size() > 1) {
            throw new JdbcFailException("일치하는 결과가 1을 초과합니다.");
        }
        return results.get(0);
    }

    public <T> T queryForObject(Connection connection, String sql, RowMapper<T> rowMapper, Object... args) {
        List<T> results = queryForObjects(connection, sql, rowMapper, args);
        if (results.isEmpty()) {
            throw new JdbcFailException("일치하는 결과가 없습니다. ");
        }
        if (results.size() > 1) {
            throw new JdbcFailException("일치하는 결과가 1을 초과합니다.");
        }
        return results.get(0);
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
