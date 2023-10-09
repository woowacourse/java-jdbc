package org.springframework.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class DataSourceTransactionManager {

    private final DataSource dataSource;

    public DataSourceTransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void doGetTransaction() {
        try {
            var connection = DataSourceUtils.getConnection(dataSource);
            connection.setAutoCommit(false);
            TransactionSynchronizationManager.setActualTransactionActive(true);
        } catch (SQLException e) {
            throw new RuntimeException("트랜잭션 시작 실패", e);
        }
    }

    public void doCommit() {
        try {
            var connection = DataSourceUtils.getConnection(dataSource);
            connection.commit();
            release(connection);
        } catch (SQLException e) {
            throw new RuntimeException("커밋 실패", e);
        }
    }

    public void doRollback() {
        try {
            var connection = DataSourceUtils.getConnection(dataSource);
            connection.rollback();
            release(connection);
        } catch (SQLException e) {
            throw new RuntimeException("롤백 실패", e);
        }
    }

    private void release(Connection connection) {
        TransactionSynchronizationManager.setActualTransactionActive(false);
        DataSourceUtils.releaseConnection(connection, dataSource);
    }
}
