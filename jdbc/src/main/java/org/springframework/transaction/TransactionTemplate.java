package org.springframework.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import org.springframework.dao.DataAccessException;

public class TransactionTemplate {

    private final Connection connection;

    public TransactionTemplate(Connection connection) {
        this.connection = connection;
    }

    public void execute(TransactionExecutor executor) {
        try {
            executor.execute(connection);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public Connection getConnection() {
        return connection;
    }

}
