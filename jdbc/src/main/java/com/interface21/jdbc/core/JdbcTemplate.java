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

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        return execute(sql, params, resultSet -> {
            if (resultSet.next()) {
                return rowMapper.mapRow(resultSet, 0);
            }
            return null;
        });
    }

    public <T> List<T> queryForList(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        return execute(sql, params, resultSet -> {
            List<T> result = new ArrayList<>();
            int rowNum = 0;
            while (resultSet.next()) {
                T row = rowMapper.mapRow(resultSet, rowNum++);
                result.add(row);
            }
            return result;
        });
    }

    public void update(final String sql, final Object... params) {
        execute(sql, params, null);
    }

    private <T> T execute(
            final String sql,
            final Object[] params,
            final ResultSetHandler<T> resultSetHandler
    ) {
        log.debug("query : {}", sql);
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            setParameters(preparedStatement, params);
            if (resultSetHandler == null) {
                preparedStatement.executeUpdate();
                return null;
            }
            resultSet = preparedStatement.executeQuery();
            return resultSetHandler.handle(resultSet);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            JdbcResourceCloser.close(connection, preparedStatement, resultSet);
        }
    }

    private void setParameters(PreparedStatement preparedStatement, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            preparedStatement.setObject(i + 1, params[i]);
        }
    }
}
