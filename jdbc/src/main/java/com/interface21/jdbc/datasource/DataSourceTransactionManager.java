package com.interface21.jdbc.datasource;

import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

/**
 * Spring의 DataSourceTransactionManager는 Spring의 TransactionSynchronizationManager를 사용함.
 * 현재 커스텀 TransactionSynchronizationManager를 사용해야 하기 때문에, Spring의 DataSourceTransactionManager를 사용할 수 없음. Connection 공유가 안됨.
 * 따라서, DataSourceTransactionManager를 직접 구현.
 * @see <a href="https://github.com/spring-projects/spring-framework/blob/main/spring-jdbc/src/main/java/org/springframework/jdbc/datasource/DataSourceTransactionManager.java">Spring DataSourceTransactionManager</a>
 */
public class DataSourceTransactionManager implements TransactionManager {

    private final DataSource dataSource;

    public DataSourceTransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void begin() {
        try {
            Connection connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            TransactionSynchronizationManager.bindResource(dataSource, connection);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to begin transaction", e);
        }
    }

    public void commit() {
        Connection connection = TransactionSynchronizationManager.getResource(dataSource);
        if (connection == null) {
            throw new IllegalStateException("No active transaction");
        }
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to commit transaction", e);
        } finally {
            releaseConnection(connection);
        }
    }

    public void rollback() {
        Connection connection = TransactionSynchronizationManager.getResource(dataSource);
        if (connection == null) {
            return;
        }
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to rollback transaction", e);
        } finally {
            releaseConnection(connection);
        }
    }

    private void releaseConnection(Connection connection) {
        TransactionSynchronizationManager.unbindResource(dataSource);
        try {
            connection.setAutoCommit(true);
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to release connection", e);
        }
    }
}
