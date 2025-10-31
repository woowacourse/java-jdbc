// jdbc/src/main/java/com/interface21/transaction/support/TransactionTemplate.java
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
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);

            TransactionSynchronizationManager.bindResource(dataSource, connection);

            try {
                work.execute();

                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw new DataAccessException("트랜잭션 수행 중 오류가 발생하여 롤백합니다.", e);
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        } finally {
            try {
                TransactionSynchronizationManager.unbindResource(dataSource);
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                throw new DataAccessException("Connection 닫기 실패", e);
            }
        }
    }
}
