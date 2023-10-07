package org.springframework.transaction;

import org.springframework.jdbc.exception.DatabaseResourceException;
import org.springframework.transaction.support.Transaction;

import javax.sql.DataSource;
import java.sql.SQLException;

public class TransactionManager {

    private final DataSource dataSource;

    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Transaction getTransaction() {
        try {
            return new Transaction(dataSource.getConnection());
        } catch (SQLException e) {
            throw new DatabaseResourceException("Database access error.", e);
        }
    }
}
