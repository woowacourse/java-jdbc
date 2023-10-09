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

    public <T> Object service(TransactionExecutor<T> transactionExecutor) throws SQLException {
        Connection conn = dataSource.getConnection();
        conn.setAutoCommit(false);
        try {
            T result = transactionExecutor.execute();
            conn.commit();
            conn.setAutoCommit(true);
            return result;
        } catch (SQLException | DataAccessException e) {
            conn.rollback();
            conn.setAutoCommit(true);
            throw new DataAccessException(e);
        } finally {
            conn.close();
        }
    }
}
