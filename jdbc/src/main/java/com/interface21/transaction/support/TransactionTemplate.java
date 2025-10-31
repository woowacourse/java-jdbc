package com.interface21.transaction.support;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class TransactionTemplate {

    private final DataSource dataSource;

    public TransactionTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(final TransactionalWork work) {
        Connection connection = null;

        DataAccessException originalException = null;

        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);

            TransactionSynchronizationManager.bindResource(dataSource, connection);

            try {
                work.execute();
                connection.commit();
            } catch (Exception e) {
                connection.rollback();

                if (e instanceof DataAccessException) {
                    originalException = (DataAccessException) e;
                } else {
                    originalException = new DataAccessException("트랜잭션 수행 중 오류가 발생하여 롤백합니다.", e);
                }
            }
        } catch (SQLException e) {
            originalException = new DataAccessException("커넥션 설정 중 오류가 발생했습니다.", e);
        } finally {
            try {
                TransactionSynchronizationManager.unbindResource(dataSource);
                TransactionSynchronizationManager.clear();
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                if (originalException == null) {
                    originalException = new DataAccessException("Connection 닫기 실패", e);
                } else {
                    originalException.addSuppressed(e);
                }
            }
        }

        if (originalException != null) {
            throw originalException;
        }
    }
}
