package org.springframework.transaction.support;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionManager {

    private final DataSource dataSource;

    public TransactionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(final LogicExecutor logicExecutor) {
        final Connection conn = DataSourceUtils.getConnection(dataSource);
        try {
            conn.setAutoCommit(false);
            logicExecutor.run();
            conn.commit();
        } catch (Exception e) {
            rollback(conn);
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    private void rollback(final Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
