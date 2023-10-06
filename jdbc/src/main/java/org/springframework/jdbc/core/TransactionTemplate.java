package org.springframework.jdbc.core;

import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionTemplate {

    private final DataSource dataSource;

    public TransactionTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void transaction(TransactionExecutor transactionExecutor) throws SQLException {
        Connection conn = dataSource.getConnection();
        conn.setAutoCommit(false);
        try {
            transactionExecutor.execute(conn);
            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException | DataAccessException e) {
            conn.rollback();
            conn.setAutoCommit(true);
            throw new DataAccessException(e);
        } finally {
            conn.close();
        }
    }
}
