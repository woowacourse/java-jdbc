package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PreparedStatementExecutor {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public PreparedStatementExecutor(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(final PreparedStatementExecuteStrategy<T> executeStrategy, final String sql, final Object... args) {
        try (
                final Connection conn = dataSource.getConnection();
                final PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            setArguments(pstmt, args);

            log.debug("query : {}", sql);

            return executeStrategy.strategy(pstmt);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void setArguments(final PreparedStatement pstmt, final Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            final int parameterIndex = i + 1;
            pstmt.setObject(parameterIndex, args[i]);
        }
    }
}
