package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.support.DataAccessUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, Connection connection, Object... params) {
        return execute(sql, connection, PreparedStatement::executeUpdate, params);
    }

    public int update(String sql, Object... params) {
        return execute(sql, PreparedStatement::executeUpdate, params);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... params) {
        return execute(sql, statement -> {
            ResultSet resultSet = statement.executeQuery();
            List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet));
            }
            return results;
        }, params);
    }

    @Nullable
    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, @Nullable Object... params) {
        List<T> result = query(sql, rowMapper, params);
        return DataAccessUtils.getNullableSingleResult(result);
    }

    private <T> T execute(String sql, PreparedStatementCallback<T> action, Object... params) {
        log.debug("Executing SQL execute: {}", sql);

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            setParameters(statement, params);
            return action.doInPreparedStatement(statement);
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private <T> T execute(String sql, Connection connection, PreparedStatementCallback<T> action, Object... params) {
        log.debug("Executing SQL execute: {}", sql);

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            setParameters(statement, params);
            return action.doInPreparedStatement(statement);
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private void setParameters(PreparedStatement statement, Object... params) throws SQLException {
        for (int i = 0; i < params.length; ++i) {
            statement.setObject(i + 1, params[i]);
        }
    }
}
