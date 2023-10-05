package org.springframework.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;

public class TransactionManager {

    private final DataSource dataSource;

    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(TransactionTemplate transactionTemplate) {
        Connection con = null;
        try {
            con = dataSource.getConnection();
            con.setAutoCommit(false);
            transactionTemplate.execute(con);
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
