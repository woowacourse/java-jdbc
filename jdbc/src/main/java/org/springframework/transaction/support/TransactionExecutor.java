package org.springframework.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.function.Supplier;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class TransactionExecutor {

    private TransactionExecutor() {
    }

    public static Optional<Object> execute(DataSource dataSource, Supplier<Object> supplier) {
        Connection conn = DataSourceUtils.getConnection(dataSource);

        try {
            conn.setAutoCommit(false);
            Object returnValue = supplier.get();
            conn.commit();
            return Optional.ofNullable(returnValue);
        } catch (RuntimeException | SQLException e) {
            rollback(conn, e);
            throw new DataAccessException(e);
        } finally {
            close(dataSource);
        }
    }

    private static void rollback(Connection conn, Exception e) {
        if (conn == null) {
            return;
        }
        try {
            conn.rollback();
        } catch (SQLException ignored) {
            throw new DataAccessException("롤백 실패", e);
        }
    }

    private static void close(DataSource dataSource) {
        try {
            DataSourceUtils.releaseConnection(dataSource);
        } catch (CannotGetJdbcConnectionException e) {
        }
    }
}
