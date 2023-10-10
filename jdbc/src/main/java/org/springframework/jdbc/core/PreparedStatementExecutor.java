package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PreparedStatementExecutor {

    private static final Logger log = LoggerFactory.getLogger(PreparedStatementExecutor.class);

    private final DataSource dataSource;

    public PreparedStatementExecutor(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(
            final PreparedStatementExecuteStrategy<T> executeStrategy,
            final String sql,
            final Object... args
    ) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        return execute(connection, executeStrategy, sql, args);
    }

    public <T> T execute(
            final Connection connection,
            final PreparedStatementExecuteStrategy<T> executeStrategy,
            final String sql,
            final Object... args
    ) {
        try (final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            setArguments(pstmt, args);

            log.debug("query : {}", sql);

            return executeStrategy.strategy(pstmt);
        } catch (SQLException e) {
            log.error("exception : {}", e);
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private void setArguments(final PreparedStatement pstmt, final Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            final int parameterIndex = i + 1;
            pstmt.setObject(parameterIndex, args[i]);
        }
    }
}
