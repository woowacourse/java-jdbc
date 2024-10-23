package com.interface21.jdbc.core;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        return query(sql, defaultPreparedStatementSetter(params), resultSet -> {
            if (resultSet.next()) {
                return rowMapper.mapRow(resultSet, resultSet.getRow());
            }
            return null;
        });
    }

    public <T> List<T> queryForList(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        return query(sql, defaultPreparedStatementSetter(params), resultSet -> {
            List<T> result = new ArrayList<>();
            while (resultSet.next()) {
                T row = rowMapper.mapRow(resultSet, resultSet.getRow());
                result.add(row);
            }
            return result;
        });
    }

    public <T> T query(
            final String sql,
            final PreparedStatementSetter preparedStatementSetter,
            final ResultSetExtractor<T> resultSetExtractor
    ) {
        return execute(sql, preparedStatementSetter, resultSetExtractor);
    }

    public void update(final String sql, final Object... params) {
        execute(sql, defaultPreparedStatementSetter(params), null);
    }

    public void update(final String sql, final PreparedStatementSetter preparedStatementSetter) {
        execute(sql, preparedStatementSetter, null);
    }

    public <T> T execute(
            final String sql,
            final Object[] params,
            final ResultSetExtractor<T> resultSetExtractor
    ) {
        return execute(sql, defaultPreparedStatementSetter(params), resultSetExtractor);
    }

    public <T> T execute(
            final String sql,
            final PreparedStatementSetter preparedStatementSetter,
            final ResultSetExtractor<T> resultSetExtractor
    ) {
        log.debug("query : {}", sql);
        final var connection = DataSourceUtils.getConnection(dataSource);
        try (final var preparedStatement = connection.prepareStatement(sql)) {
            preparedStatementSetter.setValues(preparedStatement);
            if (resultSetExtractor == null) {
                preparedStatement.executeUpdate();
                return null;
            }
            final var resultSet = preparedStatement.executeQuery();
            return resultSetExtractor.extract(resultSet);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException("Failed to execute sql: %s".formatted(sql), e);
        } finally {
            try {
                if (connection.getAutoCommit()) {
                    DataSourceUtils.releaseConnection(connection, dataSource);
                }
            } catch (SQLException e) {
                throw new DataAccessException("Failed to release connection.", e);
            }
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
