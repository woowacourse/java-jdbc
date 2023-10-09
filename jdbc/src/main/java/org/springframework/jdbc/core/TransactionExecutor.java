package org.springframework.jdbc.core;

import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.SQLException;

public class TransactionExecutor {

    private final DataSource dataSource;

    public TransactionExecutor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(TransactionalOperation operation) {
        Transaction transaction = getTransaction();
        try {
            transaction.begin();
            operation.run(transaction);
            transaction.commit();
        } catch (RuntimeException exception){
            transaction.rollback();
            throw exception;
        }
    }

    private Transaction getTransaction() {
        try {
            return new Transaction(dataSource.getConnection());
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
