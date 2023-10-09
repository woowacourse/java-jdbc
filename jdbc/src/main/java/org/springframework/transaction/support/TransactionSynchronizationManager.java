package org.springframework.transaction.support;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.ExecuteQueryCallback;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = ThreadLocal.withInitial(HashMap::new);
    private static final ThreadLocal<Boolean> isActive = ThreadLocal.withInitial(() -> false);

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource dataSource) throws SQLException {
        if (resources.get().containsKey(dataSource)) {
            return resources.get().get(dataSource);
        }
        final Connection connection = dataSource.getConnection();
        bindResource(dataSource, connection);
        return connection;
    }

    public static void bindResource(DataSource key, Connection value) {
        resources.get().put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        return resources.get().remove(key);
    }

    public static void startTransaction() {
        isActive.set(true);
    }

    public static void finishTransaction() {
        isActive.set(false);
    }

    public static <T> T commit(final ExecuteQueryCallback<T> callBack, final PreparedStatement preparedStatement, final DataSource dataSource) {
        try {
            final Connection connection = DataSourceUtils.getConnection(dataSource);
            connection.setAutoCommit(false);

            final T result = callBack.execute(preparedStatement);

            commitTransaction(dataSource, connection);

            return result;
        } catch (SQLException e) {
            throw new CannotGetJdbcConnectionException("jdbc 연결에 실패했습니다.");
        }
    }

    private static void commitTransaction(final DataSource dataSource, final Connection connection) {
        try {
            if (!isActive.get()) {
                connection.commit();

                clear(connection, dataSource);
            }
        } catch (Exception ex) {
            rollback(dataSource);

            throw new DataAccessException("실행 중 예외가 발생했습니다.");
        }
    }

    public static void rollback(final DataSource dataSource) {
        try {
            if (!isActive.get()) {
                final Connection connection = resources.get().get(dataSource);
                connection.rollback();

                clear(connection, dataSource);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("트랜잭션 롤백 중 예외가 발생했습니다.");
        }
    }

    private static void clear(final Connection connection, final DataSource dataSource) {
        try {
            connection.close();
            DataSourceUtils.releaseConnection(connection, dataSource);
        } catch (SQLException e) {
            throw new DataAccessException("실행 중 예외가 발생했습니다.");
        }
    }
}
