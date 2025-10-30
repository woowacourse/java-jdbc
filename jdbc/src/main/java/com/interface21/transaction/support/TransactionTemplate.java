package com.interface21.transaction.support;

import com.interface21.dao.DataAccessException;
import java.sql.SQLException;
import javax.sql.DataSource;

public class TransactionTemplate {

    private final DataSource dataSource;

    public TransactionTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(final TransactionalWork work) {
        try (final var connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                work.execute(connection);
                
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw new DataAccessException("트랜잭션 수행 중 오류가 발생하여 롤백합니다.", e);
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
