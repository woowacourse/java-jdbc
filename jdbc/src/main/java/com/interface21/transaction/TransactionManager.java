package com.interface21.transaction;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class TransactionManager {

    private final DataSource dataSource;

    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void executeTransaction(final TransactionalAction action) {
        try (Connection connection = dataSource.getConnection()) {
            // transaction start
            connection.setAutoCommit(false);
            try {
                action.execute(connection);
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw new DataAccessException("트랜잭션 오류발생으로 롤백", e);
            }
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException("커넥션 오류 발생", e);
        }
    }
}
