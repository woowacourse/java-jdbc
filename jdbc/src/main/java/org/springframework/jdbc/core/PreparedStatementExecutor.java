package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.CannotGetJdbcConnectionException;

public class PreparedStatementExecutor {
    private static final Logger log = LoggerFactory.getLogger(PreparedStatementExecutor.class);

    private final DataSource dataSource;

    public PreparedStatementExecutor(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(final String sql, final PreparedStatementFunction<T> pstmtFunction, final Object... params) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            setPreparedStatement(pstmt, params);
            return pstmtFunction.apply(pstmt);
        } catch (final SQLException e) {
            throw new CannotGetJdbcConnectionException(e.getMessage());
        }
    }

    public <T> T execute(final Connection conn,
                         final String sql,
                         final PreparedStatementFunction<T> pstmtFunction,
                         final Object... params) {
        try (final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            setPreparedStatement(pstmt, params);
            return pstmtFunction.apply(pstmt);
        } catch (final SQLException e) {
            throw new CannotGetJdbcConnectionException(e.getMessage());
        }
    }

    private void setPreparedStatement(final PreparedStatement pstmt, final Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
    }
}
