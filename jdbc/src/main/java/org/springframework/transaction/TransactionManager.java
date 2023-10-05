package org.springframework.transaction;

import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;

public class TransactionManager {

    private final DataSource dataSource;

    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Transaction getTransaction() {
        try {
            return new Transaction(dataSource.getConnection());
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

}
