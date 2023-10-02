package org.springframework.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;

public class TransactionalExecutor {

    private final DataSource dataSource;

    public TransactionalExecutor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(TransactionTask transactionTask) {
        Connection con = getTransactionalConnection();
        try {
            transactionTask.execute(con);
        } catch (Exception e) {
            rollback(con);
            throw e;
        } finally {
            commitAndClose(con);
        }
    }

    private Connection getTransactionalConnection() {
        try {
            Connection con = dataSource.getConnection();
            con.setAutoCommit(false);
            return con;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void rollback(Connection con)  {
        try {
            con.rollback();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void commitAndClose(Connection con) {
        try {
            con.commit();
            con.close();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
