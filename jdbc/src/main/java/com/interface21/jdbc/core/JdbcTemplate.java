package com.interface21.jdbc.core;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    public static final int ALLOWED_RESULTS = 1;

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, Object... params) {
        return update(sql, new ArgumentPreparedStatementSetter(params));
    }

    public int update(String sql, PreparedStatementSetter preparedStatementSetter) {
        return execute(sql, statement -> {
            preparedStatementSetter.setValues(statement);
            return statement.executeUpdate();
        });
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        return queryForObject(sql, rowMapper, new ArgumentPreparedStatementSetter(params));
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, PreparedStatementSetter preparedStatementSetter) {
        List<T> result = query(sql, rowMapper, preparedStatementSetter);
        if (result.size() != ALLOWED_RESULTS) {
            throw new DataAccessException("Expected " + ALLOWED_RESULTS + " result, but found  " + result.size() + " for query: " + sql);
        }
        return result.getFirst();
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... params) {
        return query(sql, rowMapper, new ArgumentPreparedStatementSetter(params));
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, PreparedStatementSetter preparedStatementSetter) {
        return execute(sql, statement -> {
            preparedStatementSetter.setValues(statement);
            return queryForAll(statement, rowMapper);
        });
    }

    private <T> T execute(String sql, PreparedStatementCallback<T> callback) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            return callback.doInStatement(statement);
        } catch (SQLException e) {
            throw new DataAccessException("Error executing SQL: " + sql, e);
        } finally {
            if (DataSourceUtils.isTransactionNotActive(connection, dataSource)) {
                DataSourceUtils.releaseConnection(connection, dataSource);
            }
        }
    }

    private <T> List<T> queryForAll(PreparedStatement statement, RowMapper<T> rowMapper) throws SQLException {
        ResultSet resultSet = statement.executeQuery();
        List<T> result = new ArrayList<>();
        while (resultSet.next()) {
            result.add(rowMapper.mapRow(resultSet));
        }
        return result;
    }
}
