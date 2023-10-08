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

    public void execute(TransactionExecuter executer) {
        Connection conn = getConnection();

        try (conn) {
            conn.setAutoCommit(false);

            executer.execute(conn);

            conn.commit();
        } catch (SQLException e) {
            rollback(conn);
        }
    }

    private Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void rollback(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException ex) {
            throw new DataAccessException(ex);
        }
    }
}
