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
        changeAutoCommitStatus(con, false);
        try {
            transactionTask.execute(con);
            changeAutoCommitStatus(con, true);
        } catch (Exception e) {
            rollback(con);
            throw e;
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    private void changeAutoCommitStatus(Connection con, boolean status) {
        try {
            con.setAutoCommit(status);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void rollback(Connection con)  {
        try {
            con.rollback();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
