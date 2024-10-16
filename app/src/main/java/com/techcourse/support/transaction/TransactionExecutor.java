package com.techcourse.support.transaction;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.techcourse.config.DataSourceConfig;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

public class TransactionExecutor {

    private TransactionExecutor() {
    }

    public static void executeTransaction(Runnable action) {
        executeTransaction(() -> {
            action.run();
            return null;
        });
    }

    public static <T> T executeTransaction(Supplier<T> action) {
        DataSource dataSource = DataSourceConfig.getInstance();
        Connection connection = DataSourceUtils.getConnection(dataSource);

        try {
            return commit(action, connection);
        } catch (SQLException e) {
            rollback(e, connection);
            throw new DataAccessException("트랜잭션 수행 실패", e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
            setAutoCommitTrue(connection);
        }
    }

    private static <T> T commit(Supplier<T> action, Connection connection) throws SQLException {
        connection.setAutoCommit(false);
        T result = action.get();
        connection.commit();
        return result;
    }

    private static void rollback(SQLException e, Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException exception) {
            throw new DataAccessException("트랜잭션 롤백 실패", e);
        }
    }

    private static void setAutoCommitTrue(Connection connection) {
        try {
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new DataAccessException("auto-commit 설정 실패", e);
        }
    }
}
