package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

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

    public int update(final String sql, final Object... args) {
        return execute(sql, new PrepareStatementUpdateExecutor(), args);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return execute(sql, new PrepareStatementQueryExecutor<T>(rowMapper), args);
    }

    @Nullable
    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final List<T> results = query(sql, rowMapper, args);
        if (results.size() != 1) {
            throw new IncorrectResultSizeDataAccessException(1);
        }
        return results.iterator().next();
    }

    private <T> T execute(String sql, PrepareStatementExecutor<T> executor, Object... args) {
        final Connection connection = getConnection();
        try (final PreparedStatement preparedStatement = createPreparedStatementSetter(connection, sql, args)) {
            return executor.execute(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage());
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private PreparedStatement createPreparedStatementSetter(final Connection connection, final String sql, final Object[] args) throws SQLException {
        final PreparedStatement preparedStatement = connection.prepareStatement(sql);
        for (int i = 0; i < args.length; i++) {
            preparedStatement.setObject(i + 1, args[i]);
        }
        return preparedStatement;
    }

    private Connection getConnection() {
        return DataSourceUtils.getConnection(dataSource);
    }
}
