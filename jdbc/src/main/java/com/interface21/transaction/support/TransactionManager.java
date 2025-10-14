package com.interface21.transaction.support;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class TransactionManager {
    private final DataSource dataSource;

    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(final TransactionCallback<T> callback) {
        try (Connection connection = dataSource.getConnection()) {
            try {
                connection.setAutoCommit(false);
                T result = callback.execute(connection);
                connection.commit();
                return result;
            } catch (Exception e) {
                connection.rollback();
                throw new DataAccessException(e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }
}
