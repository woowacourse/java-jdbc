package com.interface21.transaction.support;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class DataSourceTransactionManager implements PlatformTransactionManager {

    private final DataSource dataSource;

    public DataSourceTransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void getTransaction() throws SQLException {
        Connection conn = DataSourceUtils.getConnection(dataSource);
        conn.setAutoCommit(false);
        TransactionSynchronizationManager.bindResource(dataSource, conn);
    }

    @Override
    public void commit() throws SQLException {
        Connection conn = TransactionSynchronizationManager.getResource(dataSource);
        conn.commit();
        DataSourceUtils.releaseConnection(conn, dataSource);
    }

    @Override
    public void rollback() {
        Connection conn = TransactionSynchronizationManager.getResource(dataSource);
        try {
            conn.rollback();
        } catch (RuntimeException | SQLException exception) {
            throw new DataAccessException(exception.getMessage());
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }
}
