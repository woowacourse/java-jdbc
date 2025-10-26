package com.interface21.transaction;

import com.interface21.dao.DataAccessException;
import java.sql.SQLException;
import javax.sql.DataSource;

public class TransactionManager {

    private final DataSource dataSource;

    public TransactionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void executeTransaction(final TransactionalAction action) {
        try (final var connection = dataSource.getConnection();) {
            connection.setAutoCommit(false);

            try {
                action.execute(connection);
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw new DataAccessException("트랜잭션 내부 작업 수행 중 오류가 발생하여 롤백이 수행되었습니다.", e);
            }

        } catch (SQLException e) {
            throw new RuntimeException("트랜잭션 로직을 실행하는 중 Connection 오류가 발생했습니다.", e);
        }
    }
}
