package com.interface21.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

import javax.sql.DataSource;

import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;

public class TransactionManager {

    private TransactionManager() {
    }

    public static <T> T executeInTransaction(final DataSource dataSource, final Supplier<T> action)
        throws SQLException {

        // 이미 바인딩된 연결이 있는지 확인 (중첩 트랜잭션 감지)
        Connection existingConnection = TransactionSynchronizationManager.getResource(dataSource);
        boolean isNewTransaction = isNewTransaction(existingConnection);

        Connection connection = DataSourceUtils.getConnection(dataSource);
        boolean originAutoCommit = connection.getAutoCommit();

        if (isNewTransaction) {
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            connection.setAutoCommit(false);
        }

        try {
            T result = action.get();
            if (isNewTransaction) {
                connection.commit();
            }
            return result;
        } catch (Exception e) {
            if (isNewTransaction) {
                connection.rollback();
            }
            throw e;
        } finally {
            if (isNewTransaction) {
                connection.setAutoCommit(originAutoCommit);
                TransactionSynchronizationManager.unbindResource(dataSource);
                DataSourceUtils.releaseConnection(connection, dataSource);
            }
        }
    }

    private static boolean isNewTransaction(Connection existingConnection) {
        return existingConnection == null;
    }
}
