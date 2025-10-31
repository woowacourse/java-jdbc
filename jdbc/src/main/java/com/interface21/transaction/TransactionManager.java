package com.interface21.transaction;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class TransactionManager {

    private final DataSource dataSource;

    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void executeTransaction(final TransactionalAction action) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        boolean originAutoCommit = false;
        try {
            originAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            action.execute(connection);
            connection.commit();
        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                e.addSuppressed(ex);
            }
            throw new DataAccessException("트랜잭션 수행 중 오류 발생으로 롤백 시도", e);
        } finally {
            try {
                connection.setAutoCommit(originAutoCommit);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            TransactionSynchronizationManager.unbindResource(dataSource);
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }
}
