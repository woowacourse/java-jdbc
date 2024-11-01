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

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        return executeQuery(sql, preparedStatement -> {
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return rowMapper.mapRow(resultSet, resultSet.getRow());
            }
            return null;
        }, params);
    }

    public int update(String sql, Object... params) {
        return executeQuery(sql, PreparedStatement::executeUpdate, params);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        return executeQuery(sql, preparedStatement -> {
            List<T> instances = new ArrayList<>();
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                T instance = rowMapper.mapRow(resultSet, resultSet.getRow());
                instances.add(instance);
            }
            return instances;
        });
    }

    private <T> T executeQuery(String sql, QueryExecutor<PreparedStatement, T> action, Object... params) {
        Connection connection = DataSourceUtils.getConnection(this.dataSource);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setParams(params, preparedStatement);

            log.info("SQL : {}", sql);
            return action.execute(preparedStatement);
        } catch (SQLException e) {
            log.error("SQL error: {}", e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private void setParams(Object[] params, PreparedStatement preparedStatement) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            preparedStatement.setObject(i + 1, params[i]);
        }
    }
}
