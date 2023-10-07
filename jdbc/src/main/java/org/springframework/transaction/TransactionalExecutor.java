package org.springframework.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class TransactionalExecutor {

    private final DataSource dataSource;

    public TransactionalExecutor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(TransactionTask transactionTask) {
        Connection con = DataSourceUtils.getConnection(dataSource);
        autoCommitFalse(con);
        try {
            transactionTask.execute(con);
            commit(con);
        } catch (Exception e) {
            rollback(con);
            throw e;
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    private void autoCommitFalse(Connection con) {
        try {
            con.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void commit(Connection con) {
        try {
            con.commit();
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

    private void close(Connection con) {
        try {
            con.close();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
