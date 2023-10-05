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
        Connection con = null;
        try {
            con = dataSource.getConnection();
            con.setAutoCommit(false);
            transactionTask.execute(con);
            con.commit();
        } catch (SQLException e) {
            throw new DataAccessException();
        } finally {
            if (con != null) {
                rollback(con);
            }
        }
    }

    private void rollback(Connection con) {
        try {
            con.rollback();
            con.close();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
