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
        setAutoCommit(con, false);
        try {
            transactionTask.execute(con);
        } catch (Exception e) {
            rollback(con);
            throw e;
        } finally {
            close(con);
        }
    }

    private void setAutoCommit(Connection con, boolean status) {
        try {
            con.setAutoCommit(status);
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
        setAutoCommit(con, true);
        DataSourceUtils.releaseConnection(con, dataSource);
    }
}
