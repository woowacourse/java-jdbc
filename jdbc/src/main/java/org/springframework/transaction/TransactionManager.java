package org.springframework.transaction;

import java.sql.Connection;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;

public class TransactionManager {

    private final DataSource dataSource;

    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(TransactionTemplate transactionTemplate) {
        try (Connection connection = dataSource.getConnection()) {
            try {
                connection.setAutoCommit(false);
                transactionTemplate.execute(connection);
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw new DataAccessException();
            }
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
    }
}
