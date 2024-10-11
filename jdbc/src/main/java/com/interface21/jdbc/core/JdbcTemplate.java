package com.interface21.jdbc.core;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.interface21.dao.DataAccessException;
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
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            preparedStatementSetter.setValues(statement);
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error insert sql: " + sql, e);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        return queryForObject(sql, rowMapper, new ArgumentPreparedStatementSetter(params));
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, PreparedStatementSetter preparedStatementSetter) {
        List<T> result = query(sql, rowMapper, preparedStatementSetter);
        if (result.size() != ALLOWED_RESULTS) {
            throw new DataAccessException("Expected " + ALLOWED_RESULTS + " result, but found  "+ result.size() + " for query: " + sql);
        }
        return result.getFirst();
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... params) {
        return query(sql, rowMapper, new ArgumentPreparedStatementSetter(params));
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, PreparedStatementSetter preparedStatementSetter) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            preparedStatementSetter.setValues(statement);
            return queryForAll(statement, rowMapper);
        } catch (SQLException e) {
            throw new DataAccessException("Error insert sql: " + sql, e);
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
