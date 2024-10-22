package com.interface21.jdbc.manager;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionManager {

    private static final String TRANSACTION_FAIL_EXCEPTION = "Transaction을 실행하던 도중 실패했습니다.";
    private static final String CONNECTION_FAIL_EXCEPTION = "Connection을 연결하던 도중 실패했습니다.";

    private final DataSource dataSource;

    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void doBegin(DataSource dataSource) {
        try {
            Connection connection = DataSourceUtils.getConnection(dataSource);
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new DataAccessException(CONNECTION_FAIL_EXCEPTION);
        }
    }

    public void doCommit(DataSource dataSource) {
        try {
            Connection connection = DataSourceUtils.getConnection(dataSource);
            connection.commit();
        } catch (SQLException e) {
            throw new DataAccessException(TRANSACTION_FAIL_EXCEPTION);
        }
    }

    public void doRollback(DataSource dataSource) {
        try {
            Connection connection = DataSourceUtils.getConnection(dataSource);
            connection.rollback();
        } catch (SQLException e) {
            throw new DataAccessException(TRANSACTION_FAIL_EXCEPTION);
        }
    }

    public void doClose(DataSource dataSource) {
        Connection connection = DataSourceUtils.getConnection(dataSource);

        try {
            connection.close();
            TransactionSynchronizationManager.unbindResource(dataSource);
        } catch (SQLException e) {
            throw new DataAccessException(TRANSACTION_FAIL_EXCEPTION);
        }
    }

    public DataSource getDatasource() {
        return dataSource;
    }
}
