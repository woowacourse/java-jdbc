package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.function.Function;

public class JdbcExecutor {

    private static final Logger log = LoggerFactory.getLogger(JdbcExecutor.class);

    private final DataSource dataSource;

    public JdbcExecutor(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(
            final String sql,
            final Object[] args,
            final Function<PreparedStatement, T> action
    ) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);

            processPreparedStatementParameter(pstmt, args);

            return action.apply(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void processPreparedStatementParameter(
            final PreparedStatement pstmt,
            final Object[] args
    ) throws SQLException {
        final int argsLength = args.length;
        for (int i = 0; i < argsLength; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }
}
