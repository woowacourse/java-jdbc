package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, Object... args) {
        return execute(sql, new PrepareStatementUpdateExecutor(), args);
    }

    public int update(Connection connection, String sql, Object... args) {
        return execute(sql, connection, new PrepareStatementUpdateExecutor(), args);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        return execute(sql, new PrepareStatementQueryExecutor<>(rowMapper), args);
    }

    @Nullable
    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        return execute(sql, new PrepareStatementQueryForObjectExecutor<>(rowMapper), args);
    }

    private <T> T execute(String sql, Connection connection, PrepareStatementExecutor<T> executor, Object... args) {
        try (PreparedStatement preparedStatement = getPreparedStatement(sql, connection, args)) {
            return executor.execute(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> T execute(String sql, PrepareStatementExecutor<T> executor, Object... args) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = getPreparedStatement(sql, connection, args)
        ) {
            return executor.execute(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private PreparedStatement getPreparedStatement(final String sql, final Connection connection, final Object... args) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            for (int i = 0; i < args.length; i++) {
                preparedStatement.setObject(i + 1, args[i]);
            }
            return preparedStatement;
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
