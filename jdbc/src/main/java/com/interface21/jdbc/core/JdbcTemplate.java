package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.interface21.dao.DataAccessException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        return execute(sql, params, resultSet -> {
            if (resultSet.next()) {
                return rowMapper.mapRow(resultSet, resultSet.getRow());
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
        execute(sql, defaultPreparedStatementSetter(params), null);
    }

    public <T> T execute(
            final String sql,
            final Object[] params,
            final ResultSetHandler<T> resultSetHandler
    ) {
        return execute(sql, defaultPreparedStatementSetter(params), resultSetHandler);
    }

    public <T> T execute(
            final String sql,
            final PreparedStatementSetter preparedStatementSetter,
            final ResultSetHandler<T> resultSetHandler
    ) {
        log.debug("query : {}", sql);
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatementSetter.setValues(preparedStatement);
            if (resultSetHandler == null) {
                preparedStatement.executeUpdate();
                return null;
            }
            resultSet = preparedStatement.executeQuery();
            return resultSetHandler.handle(resultSet);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        } finally {
            JdbcResourceCloser.close(connection, preparedStatement, resultSet);
        }
    }

    private PreparedStatementSetter defaultPreparedStatementSetter(final Object... params) {
        return preparedStatement -> {
            for (int i = 0; Objects.nonNull(params) && i < params.length; i++) {
                preparedStatement.setObject(i + 1, params[i]);
            }
        };
    }
}
