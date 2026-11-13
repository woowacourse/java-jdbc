package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, Object... args) {
        return execute(sql, PreparedStatement::executeUpdate, args);
    }

    public <T> T queryForObject(
            String sql,
            RowMapper<T> rowMapper,
            Object... args
    ) {
        List<T> results = query(sql, rowMapper, args);

        if (results.isEmpty()) {
            return null;
        }
        return results.getFirst();
    }

    public <T> List<T> query(
            String sql,
            RowMapper<T> rowMapper,
            Object ... args
    ) {
        return execute(sql, statement -> {
            try (ResultSet resultSet = statement.executeQuery()) {
                List<T> results = new ArrayList<>();

                while (resultSet.next()) {
                    results.add(rowMapper.mapRow(resultSet));
                }

                return results;
            }
        }, args);
    }

    private <T> T execute(
            String sql,
            PreparedStatementCallback<T> callback,
            Object... args
    ) {
        final var connection = DataSourceUtils.getConnection(dataSource);
        Throwable failure = null;
        try {
            return execute(connection, sql, callback, args);
        } catch (RuntimeException | Error e) {
            failure = e;
            throw e;
        } finally {
            try {
                DataSourceUtils.releaseConnection(connection, dataSource);
            } catch (RuntimeException releaseException) {
                if (failure == null) {
                    throw releaseException;
                }
                failure.addSuppressed(releaseException);
            }
        }
    }

    private <T> T execute(
            Connection connection,
            String sql,
            PreparedStatementCallback<T> callback,
            Object... args
    ) {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            log.debug("query : {}", sql);

            setParameters(statement, args);
            return callback.doInPreparedStatement(statement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException("SQL 실행에 실패했습니다: " + sql, e);
        }
    }

    private void setParameters(
            PreparedStatement statement,
            Object... args
    ) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            statement.setObject(i + 1, args[i]);
        }
    }
}
