package com.techcourse.support.jdbc;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class CustomTransactionManager {

    private final DataSource dataSource;

    public CustomTransactionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void executeTransaction(TransactionCallback transactionCallback) {
        try (Connection connection = DataSourceUtils.getConnection(dataSource)) {
            connection.setAutoCommit(false);
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            TransactionSynchronizationManager.activateTransaction(dataSource);
            try {
                transactionCallback.execute();
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw new DataAccessException("[ERROR] 트랜잭션 중 오류 발생", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("[ERROR] DB 연결 중 오류 발생", e);
        } finally {
            TransactionSynchronizationManager.deactivateTransaction(dataSource);
            Connection connection = TransactionSynchronizationManager.unbindResource(dataSource);
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }
}
