package org.springframework.transaction.support;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class Transaction {

    private final DataSource dataSource;

    public Transaction(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T run(final TransactionTemplate<T> transactionTemplate) {
        final Connection conn = DataSourceUtils.getConnection(dataSource);
        try {
            conn.setAutoCommit(false);

            final T result = transactionTemplate.run();

            conn.commit();
            return result;
        } catch (Exception e) {
            rollback(conn);
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    private void rollback(Connection conn) {
        try {
            conn.rollback();
            System.out.println("rollback completed");
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
}
